package edu.cit.morre.campuscare.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.morre.campuscare.R
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.model.ChangePasswordRequest
import edu.cit.morre.campuscare.model.UpdateProfileRequest
import edu.cit.morre.campuscare.utils.CustomToast
import edu.cit.morre.campuscare.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

        private lateinit var etFirstName: EditText
        private lateinit var etLastName: EditText
        private lateinit var tvEmail: TextView
        private lateinit var tvRole: TextView
        private lateinit var tvAvatar: TextView
        private lateinit var tvFullName: TextView
        private lateinit var tvEmailDisplay: TextView
        private lateinit var tvRoleBadge: TextView

        // Password fields
        private lateinit var etCurrentPassword: EditText
        private lateinit var etNewPassword: EditText
        private lateinit var etConfirmNewPassword: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_profile)

                etFirstName = findViewById(R.id.etFirstName)
                etLastName = findViewById(R.id.etLastName)
                tvEmail = findViewById(R.id.tvEmail)
                tvRole = findViewById(R.id.tvRole)
                tvAvatar = findViewById(R.id.tvAvatar)
                tvFullName = findViewById(R.id.tvFullName)
                tvEmailDisplay = findViewById(R.id.tvEmailDisplay)
                tvRoleBadge = findViewById(R.id.tvRoleBadge)
                etCurrentPassword = findViewById(R.id.etCurrentPassword)
                etNewPassword = findViewById(R.id.etNewPassword)
                etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)

                findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
                findViewById<Button>(R.id.btnSave).setOnClickListener { saveProfile() }
                findViewById<Button>(R.id.btnChangePassword).setOnClickListener { changePassword() }

                loadProfile()
        }

        private fun loadProfile() {
                lifecycleScope.launch {
                        try {
                                val api = RetrofitClient.getAuthenticatedClient(this@ProfileActivity)
                                        .create(AuthApi::class.java)
                                val result = withContext(Dispatchers.IO) { api.getProfile().execute() }

                                if (result.isSuccessful) {
                                        val profile = result.body() ?: return@launch

                                        etFirstName.setText(profile.firstName)
                                        etLastName.setText(profile.lastName)
                                        tvEmail.text = profile.email
                                        tvRole.text = profile.role

                                        val initials = buildString {
                                                if (profile.firstName.isNotEmpty()) append(profile.firstName[0].uppercaseChar())
                                                if (profile.lastName.isNotEmpty()) append(profile.lastName[0].uppercaseChar())
                                        }
                                        tvAvatar.text = initials.ifEmpty { "?" }
                                        tvFullName.text = "${profile.firstName} ${profile.lastName}"
                                        tvEmailDisplay.text = profile.email
                                        tvRoleBadge.text = profile.role
                                } else {
                                        CustomToast.error(this@ProfileActivity, "Failed to load profile")
                                }
                        } catch (e: Exception) {
                                CustomToast.error(this@ProfileActivity, "Error: ${e.message}")
                        }
                }
        }

        private fun saveProfile() {
                val firstName = etFirstName.text.toString().trim()
                val lastName = etLastName.text.toString().trim()

                if (firstName.isEmpty() || lastName.isEmpty()) {
                        CustomToast.error(this, "Please fill all fields")
                        return
                }

                lifecycleScope.launch {
                        try {
                                val api = RetrofitClient.getAuthenticatedClient(this@ProfileActivity)
                                        .create(AuthApi::class.java)
                                val result = withContext(Dispatchers.IO) {
                                        api.updateProfile(UpdateProfileRequest(firstName, lastName)).execute()
                                }

                                if (result.isSuccessful) {
                                        TokenManager.saveFirstName(this@ProfileActivity, firstName)
                                        TokenManager.saveLastName(this@ProfileActivity, lastName)

                                        val initials = buildString {
                                                if (firstName.isNotEmpty()) append(firstName[0].uppercaseChar())
                                                if (lastName.isNotEmpty()) append(lastName[0].uppercaseChar())
                                        }
                                        tvAvatar.text = initials
                                        tvFullName.text = "$firstName $lastName"

                                        CustomToast.success(this@ProfileActivity, "Profile updated successfully!")
                                } else {
                                        CustomToast.error(this@ProfileActivity, "Update failed")
                                }
                        } catch (e: Exception) {
                                CustomToast.error(this@ProfileActivity, "Error: ${e.message}")
                        }
                }
        }

        private fun changePassword() {
                val currentPassword = etCurrentPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmNewPassword.text.toString().trim()

                // Validate
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        CustomToast.error(this, "Please fill all password fields")
                        return
                }
                if (newPassword.length < 6) {
                        CustomToast.error(this, "New password must be at least 6 characters")
                        return
                }
                if (newPassword != confirmPassword) {
                        CustomToast.error(this, "Passwords do not match")
                        return
                }
                if (currentPassword == newPassword) {
                        CustomToast.error(this, "New password must be different from current")
                        return
                }

                lifecycleScope.launch {
                        try {
                                val api = RetrofitClient.getAuthenticatedClient(this@ProfileActivity)
                                        .create(AuthApi::class.java)

                                val request = ChangePasswordRequest(
                                        currentPassword = currentPassword,
                                        newPassword = newPassword,
                                        confirmPassword = confirmPassword
                                )

                                android.util.Log.d("CHANGE_PASSWORD", "Sending to: profile/change-password")
                                android.util.Log.d("CHANGE_PASSWORD", "Request: $request")

                                val result = withContext(Dispatchers.IO) {
                                        api.changePassword(request).execute()
                                }

                                android.util.Log.d("CHANGE_PASSWORD", "Response code: ${result.code()}")

                                if (result.isSuccessful) {
                                        etCurrentPassword.text.clear()
                                        etNewPassword.text.clear()
                                        etConfirmNewPassword.text.clear()
                                        CustomToast.success(this@ProfileActivity, "Password updated successfully!")
                                } else {
                                        val errorBody = result.errorBody()?.string()
                                        android.util.Log.e("CHANGE_PASSWORD", "Error: $errorBody")

                                        // Parse error message if available
                                        val errorMessage = try {
                                                if (errorBody != null) {
                                                        org.json.JSONObject(errorBody).optString("message", "Failed to change password")
                                                } else {
                                                        "Failed to change password"
                                                }
                                        } catch (e: Exception) {
                                                "Failed to change password. Please check your current password."
                                        }

                                        CustomToast.error(this@ProfileActivity, errorMessage)
                                }
                        } catch (e: Exception) {
                                android.util.Log.e("CHANGE_PASSWORD", "Exception: ${e.message}", e)
                                CustomToast.error(this@ProfileActivity, "Network error: ${e.message}")
                        }
                }
        }
}