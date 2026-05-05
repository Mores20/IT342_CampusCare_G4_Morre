package edu.cit.morre.campuscare.auth

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.morre.campuscare.R
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.auth.RetrofitClient
import edu.cit.morre.campuscare.model.AppointmentRequest
import edu.cit.morre.campuscare.model.AppointmentResponse
import edu.cit.morre.campuscare.utils.CustomToast
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

private var selectedDate = ""
private var selectedTime = ""
private var selectedFileUri: Uri? = null
private var selectedFileName = ""

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

        val etReason = findViewById<EditText>(R.id.etReason)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val btnDate = findViewById<Button>(R.id.btnPickDate)
        val btnTime = findViewById<Button>(R.id.btnPickTime)
        val tvDate = findViewById<TextView>(R.id.tvSelectedDate)
        val tvTime = findViewById<TextView>(R.id.tvSelectedTime)
        val btnFile = findViewById<Button>(R.id.btnAttachFile)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // ✅ Date Picker
        btnDate.setOnClickListener {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
        selectedDate = "$y-${String.format("%02d", m + 1)}-${String.format("%02d", d)}"
        tvDate.text = "📅 $selectedDate"
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        .show()
        }

        // ✅ Time Picker
        btnTime.setOnClickListener {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
        selectedTime = "${String.format("%02d", h)}:${String.format("%02d", m)}"
        tvTime.text = "🕐 $selectedTime"
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // ✅ File Picker
        btnFile.setOnClickListener {
        filePicker.launch("*/*")
        }

        // ✅ Submit
        btnSubmit.setOnClickListener {
        val reason = etReason.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        if (reason.isEmpty()) {
        CustomToast.info(this@BookAppointmentActivity, "Please fill all fields")
            }
                    if (selectedDate.isEmpty()) {
                    CustomToast.info(this@BookAppointmentActivity, "Please select a date")
                    return@setOnClickListener
            }
                    if (selectedTime.isEmpty()) {
                    CustomToast.info(this@BookAppointmentActivity, "Please select time")
                    return@setOnClickListener
            }

                    btnSubmit.isEnabled = false
                    btnSubmit.text = "Submitting..."

                    val api = RetrofitClient.getAuthenticatedClient(this).create(AuthApi::class.java)
        val request = AppointmentRequest(reason, selectedDate, selectedTime, notes)

        api.bookAppointment(request).enqueue(object : Callback<AppointmentResponse> {
        override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
        if (response.isSuccessful) {
        val appointmentId = response.body()?.id

        // ✅ Upload file if selected
        if (selectedFileUri != null && appointmentId != null) {
        uploadFile(appointmentId)
        } else {
        runOnUiThread {
        CustomToast.success(this@BookAppointmentActivity, "Appointment Booked")
        finish()
        }
        }
        } else {
        runOnUiThread {
        btnSubmit.isEnabled = true
        btnSubmit.text = "Submit Appointment"
        CustomToast.error(this@BookAppointmentActivity, "Booking Failed")
        }
        }
        }

        override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
        runOnUiThread {
        btnSubmit.isEnabled = true
        btnSubmit.text = "Submit Appointment"
        CustomToast.error(this@BookAppointmentActivity, "Error")
        }
        }
        })
        }
        }

private fun uploadFile(appointmentId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
        try {
        val uri = selectedFileUri ?: return@launch
                val file = uriToFile(uri)
                        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        val filePart = MultipartBody.Part.createFormData("file", selectedFileName, requestFile)
                        val apptIdBody = appointmentId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                        val api = RetrofitClient.getAuthenticatedClient(this@BookAppointmentActivity)
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
        val tempFile = File.createTempFile("upload_", "_$selectedFileName", cacheDir)
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