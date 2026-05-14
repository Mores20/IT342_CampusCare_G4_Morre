package edu.cit.morre.campuscare.features.dashboard

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.cit.morre.campuscare.R
import edu.cit.morre.campuscare.features.auth.AuthApi
import edu.cit.morre.campuscare.features.appointment.model.AppointmentRequest
import edu.cit.morre.campuscare.features.appointment.AppointmentResponse
import edu.cit.morre.campuscare.shared.ui.CustomToast
import edu.cit.morre.campuscare.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class BookAppointmentActivity : AppCompatActivity() {

        // ✅ All clinic slots — match these to your web ALL_SLOTS array
        private val allSlots = listOf(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30"
        )

        private var selectedDate = ""
        private var selectedTime = ""
        private var bookedSlots = listOf<String>()
        private var selectedFileUri: Uri? = null
        private var selectedFileName = ""

        // Track which button is currently selected so we can deselect it
        private var selectedSlotButton: Button? = null

        private val filePicker = registerForActivityResult(
                ActivityResultContracts.GetContent()
        ) { uri ->
                uri?.let {
                        selectedFileUri = it
                        selectedFileName = getFileName(it)
                        findViewById<TextView>(R.id.tvFileName).text = "📎 $selectedFileName"
                }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_book_appointment)

                val etReason     = findViewById<EditText>(R.id.etReason)
                val etNotes      = findViewById<EditText>(R.id.etNotes)
                val btnDate      = findViewById<Button>(R.id.btnPickDate)
                val tvDate       = findViewById<TextView>(R.id.tvSelectedDate)
                val btnFile      = findViewById<Button>(R.id.btnAttachFile)
                val btnSubmit    = findViewById<Button>(R.id.btnSubmit)
                val btnBack      = findViewById<Button>(R.id.btnBack)

                btnBack.setOnClickListener { finish() }
                btnFile.setOnClickListener { filePicker.launch("*/*") }

                // Date picker — fetch slots immediately after date chosen
                btnDate.setOnClickListener {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(this, { _, y, m, d ->
                                selectedDate = "$y-${String.format("%02d", m + 1)}-${String.format("%02d", d)}"
                                selectedTime = ""           // reset time when date changes
                                selectedSlotButton = null
                                tvDate.text = "📅 $selectedDate"
                                fetchBookedSlots(selectedDate)
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                                .show()
                }

                btnSubmit.setOnClickListener {
                        val reason = etReason.text.toString().trim()
                        val notes  = etNotes.text.toString().trim()

                        if (reason.isEmpty()) {
                                CustomToast.info(this, "Please enter a reason")
                                return@setOnClickListener
                        }
                        if (selectedDate.isEmpty()) {
                                CustomToast.info(this, "Please select a date")
                                return@setOnClickListener
                        }
                        if (selectedTime.isEmpty()) {
                                CustomToast.info(this, "Please select a time slot")
                                return@setOnClickListener
                        }

                        btnSubmit.isEnabled = false
                        btnSubmit.text = "Submitting..."

                        val api = RetrofitClient.getAuthenticatedClient(this).create(AuthApi::class.java)
                        val request = AppointmentRequest(reason, selectedDate, selectedTime, notes)

                        api.bookAppointment(request).enqueue(object : Callback<AppointmentResponse> {
                                override fun onResponse(
                                        call: Call<AppointmentResponse>,
                                        response: Response<AppointmentResponse>
                                ) {
                                        when {
                                                response.isSuccessful -> {
                                                        val appointmentId = response.body()?.id
                                                        if (selectedFileUri != null && appointmentId != null) {
                                                                uploadFile(appointmentId)
                                                        } else {
                                                                runOnUiThread {
                                                                        CustomToast.success(this@BookAppointmentActivity, "Appointment booked")
                                                                        finish()
                                                                }
                                                        }
                                                }
                                                response.code() == 409 -> {
                                                        // Slot was just taken — re-fetch and refresh grid
                                                        val message = try {
                                                                org.json.JSONObject(
                                                                        response.errorBody()?.string() ?: ""
                                                                ).getString("message")
                                                        } catch (e: Exception) {
                                                                "This slot was just taken. Please pick another."
                                                        }
                                                        runOnUiThread {
                                                                btnSubmit.isEnabled = true
                                                                btnSubmit.text = "Submit Appointment"
                                                                selectedTime = ""
                                                                selectedSlotButton = null
                                                                CustomToast.error(this@BookAppointmentActivity, message)
                                                                fetchBookedSlots(selectedDate) // refresh grid
                                                        }
                                                }
                                                else -> {
                                                        runOnUiThread {
                                                                btnSubmit.isEnabled = true
                                                                btnSubmit.text = "Submit Appointment"
                                                                CustomToast.error(this@BookAppointmentActivity, "Booking failed")
                                                        }
                                                }
                                        }
                                }

                                override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                                        runOnUiThread {
                                                btnSubmit.isEnabled = true
                                                btnSubmit.text = "Submit Appointment"
                                                CustomToast.error(this@BookAppointmentActivity, "Network error")
                                        }
                                }
                        })
                }
        }

        // ✅ Fetch booked slots for the chosen date, then rebuild the grid
        private fun fetchBookedSlots(date: String) {
                val badge      = findViewById<TextView>(R.id.tvAvailabilityBadge)
                val loading    = findViewById<TextView>(R.id.tvSlotsLoading)
                val slotLabel  = findViewById<TextView>(R.id.tvSlotLabel)
                val slotGrid   = findViewById<GridLayout>(R.id.slotGrid)
                val legend     = findViewById<LinearLayout>(R.id.slotLegend)

                // Show loading state
                badge.visibility   = android.view.View.GONE
                loading.visibility = android.view.View.VISIBLE
                slotGrid.visibility = android.view.View.GONE
                slotLabel.visibility = android.view.View.GONE
                legend.visibility = android.view.View.GONE

                lifecycleScope.launch(Dispatchers.IO) {
                        try {
                                val api = RetrofitClient.getAuthenticatedClient(this@BookAppointmentActivity)
                                        .create(AuthApi::class.java)
                                val response = api.getBookedSlots(date).execute()

                                withContext(Dispatchers.Main) {
                                        loading.visibility = android.view.View.GONE
                                        if (response.isSuccessful) {
                                                bookedSlots = response.body() ?: emptyList()
                                                buildSlotGrid(slotGrid)
                                                updateAvailabilityBadge(badge)
                                                slotLabel.visibility  = android.view.View.VISIBLE
                                                slotGrid.visibility   = android.view.View.VISIBLE
                                                legend.visibility     = android.view.View.VISIBLE
                                                badge.visibility      = android.view.View.VISIBLE
                                        } else {
                                                CustomToast.error(
                                                        this@BookAppointmentActivity,
                                                        "Could not load slots. Try again."
                                                )
                                        }
                                }
                        } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                        loading.visibility = android.view.View.GONE
                                        CustomToast.error(this@BookAppointmentActivity, "Network error loading slots")
                                }
                        }
                }
        }

        // ✅ Dynamically build the slot grid buttons
        private fun buildSlotGrid(grid: GridLayout) {
                grid.removeAllViews()

                // ✅ Get current time only if the selected date is today
                val calendar = Calendar.getInstance()
                val todayStr = "${calendar.get(Calendar.YEAR)}-${
                        String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${
                        String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
                val isToday = selectedDate == todayStr
                val currentHour   = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                allSlots.forEach { slot ->
                        val parts      = slot.split(":")
                        val slotHour   = parts[0].toInt()
                        val slotMinute = parts[1].toInt()

                        // ✅ Mark as past if today and slot time has already passed
                        val isPast  = isToday && (slotHour < currentHour ||
                                (slotHour == currentHour && slotMinute <= currentMinute))
                        val isTaken = bookedSlots.contains(slot)
                        val disabled = isPast || isTaken

                        val btn    = Button(this)
                        val params = GridLayout.LayoutParams().apply {
                                width  = 0
                                height = GridLayout.LayoutParams.WRAP_CONTENT
                                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                                setMargins(6, 6, 6, 6)
                        }
                        btn.layoutParams = params
                        btn.textSize     = 12f
                        btn.gravity      = Gravity.CENTER
                        btn.setPadding(4, 20, 4, 20)
                        btn.isEnabled    = !disabled

                        when {
                                isPast -> {
                                        btn.text = "${formatSlot(slot)}\nPast"
                                        btn.background = ContextCompat.getDrawable(this, R.drawable.slot_taken)
                                        btn.setTextColor(0xFFBDBDBD.toInt())
                                }
                                isTaken -> {
                                        btn.text = "${formatSlot(slot)}\nTaken"
                                        btn.background = ContextCompat.getDrawable(this, R.drawable.slot_taken)
                                        btn.setTextColor(0xFFBDBDBD.toInt())
                                }
                                else -> {
                                        btn.text = formatSlot(slot)
                                        btn.background = ContextCompat.getDrawable(this, R.drawable.slot_available)
                                        btn.setTextColor(0xFF344054.toInt())
                                        btn.setOnClickListener {
                                                selectedSlotButton?.let { prev ->
                                                        prev.background = ContextCompat.getDrawable(this, R.drawable.slot_available)
                                                        prev.setTextColor(0xFF344054.toInt())
                                                }
                                                btn.background = ContextCompat.getDrawable(this, R.drawable.slot_selected)
                                                btn.setTextColor(0xFFFFFFFF.toInt())
                                                selectedSlotButton = btn
                                                selectedTime = slot
                                        }
                                }
                        }

                        grid.addView(btn)
                }
        }

        // ✅ Update the availability badge color + text
        private fun updateAvailabilityBadge(badge: TextView) {
                val calendar = Calendar.getInstance()
                val todayStr = "${calendar.get(Calendar.YEAR)}-${
                        String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${
                        String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
                val isToday       = selectedDate == todayStr
                val currentHour   = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                val available = allSlots.count { slot ->
                        val parts      = slot.split(":")
                        val slotHour   = parts[0].toInt()
                        val slotMinute = parts[1].toInt()
                        val isPast     = isToday && (slotHour < currentHour ||
                                (slotHour == currentHour && slotMinute <= currentMinute))
                        !isPast && !bookedSlots.contains(slot)
                }

                when {
                        available == 0 -> {
                                badge.text = "⛔ No slots available on this date"
                                badge.setTextColor(0xFFA32D2D.toInt())
                                badge.setBackgroundResource(R.drawable.badge_full)
                        }
                        available <= 4 -> {
                                badge.text = "⚠️ Only $available slot${if (available == 1) "" else "s"} left"
                                badge.setTextColor(0xFF854F0B.toInt())
                                badge.setBackgroundResource(R.drawable.badge_limited)
                        }
                        else -> {
                                badge.text = "✅ $available slots available"
                                badge.setTextColor(0xFF3B6D11.toInt())
                                badge.setBackgroundResource(R.drawable.badge_background)
                        }
                }
        }

        // ✅ Format "08:00" → "8:00 AM"
        private fun formatSlot(slot: String): String {
                val parts  = slot.split(":")
                val hour   = parts[0].toInt()
                val minute = parts[1]
                val ampm   = if (hour >= 12) "PM" else "AM"
                val h      = when {
                        hour == 0    -> 12
                        hour > 12    -> hour - 12
                        else         -> hour
                }
                return "$h:$minute $ampm"
        }

        private fun uploadFile(appointmentId: Long) {
                lifecycleScope.launch(Dispatchers.IO) {
                        try {
                                val uri      = selectedFileUri ?: return@launch
                                val file     = uriToFile(uri)
                                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

                                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                                val filePart    = MultipartBody.Part.createFormData("file", selectedFileName, requestFile)
                                val apptIdBody  = appointmentId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                                val api      = RetrofitClient.getAuthenticatedClient(this@BookAppointmentActivity)
                                        .create(AuthApi::class.java)
                                val response = api.uploadFile(filePart, apptIdBody).execute()

                                withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                                CustomToast.success(this@BookAppointmentActivity, "Appointment booked and file uploaded")
                                        } else {
                                                CustomToast.info(this@BookAppointmentActivity, "Appointment booked but file upload failed")
                                        }
                                        finish()
                                }
                        } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                        CustomToast.info(this@BookAppointmentActivity, "Appointment booked but file upload failed")
                                        finish()
                                }
                        }
                }
        }

        private fun uriToFile(uri: Uri): File {
                val inputStream = contentResolver.openInputStream(uri)!!
                val tempFile    = File.createTempFile("upload_", "_$selectedFileName", cacheDir)
                FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }
                return tempFile
        }

        private fun getFileName(uri: Uri): String {
                var name = "file"
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                        if (it.moveToFirst()) {
                                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (idx >= 0) name = it.getString(idx)
                        }
                }
                return name
        }
}