package edu.cit.morre.campuscare.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.morre.campuscare.R
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.auth.RetrofitClient
import edu.cit.morre.campuscare.model.AppointmentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAppointmentsActivity : AppCompatActivity() {

private lateinit var recyclerView: RecyclerView
private lateinit var tvEmpty: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_appointments)

        recyclerView = findViewById(R.id.rvAppointments)
        tvEmpty = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        loadAppointments()
        }

    // In ViewAppointmentsActivity — replace loadAppointments() with:
    private fun loadAppointments() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthenticatedClient(this@ViewAppointmentsActivity)
                    .create(AuthApi::class.java)

                // ✅ Run network call on IO thread
                val result = withContext(Dispatchers.IO) {
                    api.getMyAppointments().execute()
                }

                if (result.isSuccessful) {
                    val appointments = result.body() ?: emptyList()
                    if (appointments.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        recyclerView.adapter = AppointmentAdapter(appointments)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewAppointmentsActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
        }

class AppointmentAdapter(private val items: List<AppointmentResponse>) :
        RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvReason.text = item.reason
        holder.tvDate.text = "📅 ${item.appointmentDate}"
        holder.tvTime.text = "🕐 ${item.appointmentTime}"
        holder.tvStatus.text = item.status

        // ✅ Color-code status
        val color = when (item.status) {
        "APPROVED"  -> android.graphics.Color.parseColor("#065f46")
        "COMPLETED" -> android.graphics.Color.parseColor("#3730a3")
        "CANCELLED" -> android.graphics.Color.parseColor("#991b1b")
        else        -> android.graphics.Color.parseColor("#b7791f") // PENDING
        }
        holder.tvStatus.setTextColor(color)
        }

        override fun getItemCount() = items.size
        }