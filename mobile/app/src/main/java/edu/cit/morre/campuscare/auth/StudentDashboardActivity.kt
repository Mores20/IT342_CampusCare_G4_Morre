package edu.cit.morre.campuscare.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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

class StudentDashboardActivity : AppCompatActivity() {

        private lateinit var rvHistory: RecyclerView
        private lateinit var tvNoUpcoming: TextView
        private lateinit var tvNoHistory: TextView
        private lateinit var upcomingCard: LinearLayout
        private lateinit var tvUpcomingReason: TextView
        private lateinit var tvUpcomingDate: TextView
        private lateinit var tvUpcomingTime: TextView
        private lateinit var tvUpcomingStatus: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_student_dashboard)

                rvHistory = findViewById(R.id.rvHistory)
                tvNoUpcoming = findViewById(R.id.tvNoUpcoming)
                tvNoHistory = findViewById(R.id.tvNoHistory)
                upcomingCard = findViewById(R.id.upcomingCard)
                tvUpcomingReason = findViewById(R.id.tvUpcomingReason)
                tvUpcomingDate = findViewById(R.id.tvUpcomingDate)
                tvUpcomingTime = findViewById(R.id.tvUpcomingTime)
                tvUpcomingStatus = findViewById(R.id.tvUpcomingStatus)

                rvHistory.layoutManager = LinearLayoutManager(this)
                rvHistory.isNestedScrollingEnabled = false

                findViewById<Button>(R.id.btnBookAppointment).setOnClickListener {
                        startActivity(Intent(this, BookAppointmentActivity::class.java))
                }
                findViewById<Button>(R.id.btnProfile).setOnClickListener {
                        startActivity(Intent(this, ProfileActivity::class.java))
                }
                findViewById<Button>(R.id.btnLogout).setOnClickListener {
                        performLogout()
                }
        }

        override fun onResume() {
                super.onResume()
                val firstName = TokenManager.getFirstName(this) ?: "Student"
                val lastName = TokenManager.getLastName(this) ?: ""
                findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $firstName $lastName 👋"
                loadAppointments()
        }

        private fun loadAppointments() {
                lifecycleScope.launch {
                        try {
                                val api = RetrofitClient.getAuthenticatedClient(this@StudentDashboardActivity)
                                        .create(AuthApi::class.java)
                                val result = withContext(Dispatchers.IO) {
                                        api.getMyAppointments().execute()
                                }

                                if (result.isSuccessful) {
                                        val all = result.body() ?: emptyList()
                                        val upcoming = all.firstOrNull {
                                                it.status == "PENDING" || it.status == "APPROVED"
                                        }
                                        val history = all.filter {
                                                it.status == "COMPLETED" || it.status == "CANCELLED"
                                        }

                                        // ✅ Show upcoming appointment
                                        if (upcoming != null) {
                                                upcomingCard.visibility = View.VISIBLE
                                                tvNoUpcoming.visibility = View.GONE
                                                tvUpcomingReason.text = upcoming.reason
                                                tvUpcomingDate.text = "📅 ${upcoming.appointmentDate}"
                                                tvUpcomingTime.text = "🕐 ${upcoming.appointmentTime}"
                                                tvUpcomingStatus.text = upcoming.status
                                                tvUpcomingStatus.setTextColor(
                                                        if (upcoming.status == "APPROVED")
                                                                android.graphics.Color.parseColor("#065f46")
                                                        else android.graphics.Color.parseColor("#b7791f")
                                                )
                                        } else {
                                                upcomingCard.visibility = View.GONE
                                                tvNoUpcoming.visibility = View.VISIBLE
                                        }

                                        // ✅ Show history
                                        if (history.isEmpty()) {
                                                tvNoHistory.visibility = View.VISIBLE
                                                rvHistory.visibility = View.GONE
                                        } else {
                                                tvNoHistory.visibility = View.GONE
                                                rvHistory.visibility = View.VISIBLE
                                                rvHistory.adapter = HistoryAdapter(history)
                                        }
                                }
                        } catch (e: Exception) {
                                CustomToast.error(this@StudentDashboardActivity, "Failed to load appointments")
                        }
                }
        }

        private fun performLogout() {
                TokenManager.clearToken(this)
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail().build()
                GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                }
        }
}

class HistoryAdapter(private val items: List<AppointmentResponse>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

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
                holder.tvStatus.setTextColor(
                        when (item.status) {
                                "COMPLETED" -> android.graphics.Color.parseColor("#3730a3")
                                "CANCELLED" -> android.graphics.Color.parseColor("#991b1b")
                                else -> android.graphics.Color.parseColor("#b7791f")
                        }
                )
        }

        override fun getItemCount() = items.size
}