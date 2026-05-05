package edu.cit.morre.campuscare.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import edu.cit.morre.campuscare.MainActivity
import edu.cit.morre.campuscare.R
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.model.AppointmentResponse
import edu.cit.morre.campuscare.utils.CustomToast
import edu.cit.morre.campuscare.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminAppointmentAdapter
    private val allAppointments = mutableListOf<AppointmentResponse>()
    private val filteredAppointments = mutableListOf<AppointmentResponse>()
    private var currentFilter = "ALL"

    private val filterLabels = listOf("ALL", "PENDING", "APPROVED", "COMPLETED", "CANCELLED")
    private val filterButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        recyclerView = findViewById(R.id.rvAdminAppointments)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminAppointmentAdapter(
            filteredAppointments,
            onStatusUpdate = { appointment, newStatus -> updateStatus(appointment.id, newStatus) },
            onViewFiles = { appointmentId -> viewFiles(appointmentId) }
        )
        recyclerView.adapter = adapter

        setupFilterTabs()

        // ✅ Logout — clears token AND signs out of Google
        findViewById<Button>(R.id.btnLogoutAdmin).setOnClickListener {
            performLogout()
        }

        loadAllAppointments()
    }

    private fun performLogout() {
        TokenManager.clearToken(this)

        // ✅ Sign out of Google so account picker shows on next login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupFilterTabs() {
        val filterContainer = findViewById<LinearLayout>(R.id.filterContainer)
        filterContainer.removeAllViews()
        filterButtons.clear()

        filterLabels.forEach { label ->
            val btn = Button(this).apply {
                text = label
                textSize = 11f
                setPadding(24, 8, 24, 8)
                stateListAnimator = null
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 12, 0) }
                setOnClickListener { applyFilter(label) }
            }
            filterButtons.add(btn)
            filterContainer.addView(btn)
        }
        updateFilterButtonStyles()
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        filteredAppointments.clear()
        filteredAppointments.addAll(
            if (filter == "ALL") allAppointments
            else allAppointments.filter { it.status == filter }
        )
        adapter.notifyDataSetChanged()
        updateFilterButtonStyles()

        findViewById<TextView>(R.id.tvNoAppointments).visibility =
            if (filteredAppointments.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateFilterButtonStyles() {
        filterButtons.forEachIndexed { index, btn ->
            val isActive = filterLabels[index] == currentFilter
            btn.setBackgroundColor(
                if (isActive) android.graphics.Color.parseColor("#0355A1")
                else android.graphics.Color.parseColor("#B2CEE8")
            )
            btn.setTextColor(
                if (isActive) android.graphics.Color.WHITE
                else android.graphics.Color.parseColor("#0355A1")
            )
        }
    }

    private fun loadAllAppointments() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthenticatedClient(this@AdminDashboardActivity)
                    .create(AuthApi::class.java)
                val result = withContext(Dispatchers.IO) { api.getAllAppointments().execute() }

                if (result.isSuccessful) {
                    val data = result.body() ?: emptyList()
                    allAppointments.clear()
                    allAppointments.addAll(data)
                    applyFilter(currentFilter)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDashboardActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatus(id: Long, newStatus: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthenticatedClient(this@AdminDashboardActivity)
                    .create(AuthApi::class.java)
                val result = withContext(Dispatchers.IO) {
                    api.updateAppointmentStatus(id, mapOf("status" to newStatus)).execute()
                }

                if (result.isSuccessful) {
                    val allIdx = allAppointments.indexOfFirst { it.id == id }
                    if (allIdx >= 0) {
                        allAppointments[allIdx] = allAppointments[allIdx].copy(status = newStatus)
                    }
                    applyFilter(currentFilter)
                    CustomToast.success(this@AdminDashboardActivity, "Status Updated to $newStatus")
                }
            } catch (e: Exception) {
                CustomToast.error(this@AdminDashboardActivity, "Error: ${e.message}")
            }
        }
    }

    private fun viewFiles(appointmentId: Long) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthenticatedClient(this@AdminDashboardActivity)
                    .create(AuthApi::class.java)
                val result = withContext(Dispatchers.IO) {
                    api.getFilesForAppointment(appointmentId).execute()
                }

                if (result.isSuccessful) {
                    val files = result.body() ?: emptyList()
                    if (files.isEmpty()) {
                        CustomToast.info(this@AdminDashboardActivity, "No files attached on this appointment")
                    } else {
                        showFilesDialog(files)
                    }
                }
            } catch (e: Exception) {
                CustomToast.error(this@AdminDashboardActivity, "Error loading files: ${e.message}")

            }
        }
    }

    private fun showFilesDialog(files: List<Map<String, Any>>) {
        val fileNames = files.map { it["fileName"]?.toString() ?: "Unknown file" }.toTypedArray()
        val fileIds = files.map { (it["id"] as? Double)?.toLong() ?: 0L }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📎 Attached Files")
            .setItems(fileNames) { _, index ->
                showFileActionDialog(fileIds[index], fileNames[index])
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showFileActionDialog(fileId: Long, fileName: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(fileName)
            .setItems(arrayOf("👁️ View", "⬇️ Download")) { _, action ->
                val inline = action == 0
                val url = "${RetrofitClient.BASE_URL}files/download/$fileId?inline=$inline"
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
                startActivity(browserIntent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class AdminAppointmentAdapter(
    private val items: MutableList<AppointmentResponse>,
    private val onStatusUpdate: (AppointmentResponse, String) -> Unit,
    private val onViewFiles: (Long) -> Unit
) : RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val tvEmail: TextView = view.findViewById(R.id.tvStudentEmail)
        val tvReason: TextView = view.findViewById(R.id.tvAdminReason)
        val tvDateTime: TextView = view.findViewById(R.id.tvAdminDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvAdminStatus)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
        val btnComplete: Button = view.findViewById(R.id.btnComplete)
        val btnViewFiles: Button = view.findViewById(R.id.btnViewFiles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = "${item.user?.firstName ?: ""} ${item.user?.lastName ?: ""}"
        holder.tvEmail.text = item.user?.email ?: ""
        holder.tvReason.text = "Reason: ${item.reason}"
        holder.tvDateTime.text = "${item.appointmentDate} at ${item.appointmentTime}"
        holder.tvStatus.text = item.status

        val color = when (item.status) {
            "APPROVED"  -> android.graphics.Color.parseColor("#065f46")
            "COMPLETED" -> android.graphics.Color.parseColor("#3730a3")
            "CANCELLED" -> android.graphics.Color.parseColor("#991b1b")
            else        -> android.graphics.Color.parseColor("#b7791f")
        }
        holder.tvStatus.setTextColor(color)

        when (item.status) {
            "PENDING" -> {
                holder.btnApprove.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnComplete.visibility = View.GONE
            }
            "APPROVED" -> {
                holder.btnApprove.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
                holder.btnComplete.visibility = View.VISIBLE
            }
            else -> {
                holder.btnApprove.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
                holder.btnComplete.visibility = View.GONE
            }
        }

        holder.btnApprove.setOnClickListener { onStatusUpdate(item, "APPROVED") }
        holder.btnCancel.setOnClickListener { onStatusUpdate(item, "CANCELLED") }
        holder.btnComplete.setOnClickListener { onStatusUpdate(item, "COMPLETED") }
        holder.btnViewFiles.setOnClickListener { onViewFiles(item.id) }
    }

    override fun getItemCount() = items.size
}