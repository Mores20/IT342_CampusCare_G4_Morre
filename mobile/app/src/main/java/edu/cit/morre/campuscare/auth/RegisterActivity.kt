package edu.cit.morre.campuscare.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.databinding.ActivityRegisterBinding
import edu.cit.morre.campuscare.model.AuthResponse
import edu.cit.morre.campuscare.model.RegisterRequest
import edu.cit.morre.campuscare.utils.CustomToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            CustomToast.info(this@RegisterActivity, "Please fill all fields")
            return
        }
        if (password != confirmPassword) {
            CustomToast.error(this@RegisterActivity, "Password mismatch")
            return
        }

        val request = RegisterRequest(firstName, lastName, email, password)
        val authApi = RetrofitClient.instance.create(AuthApi::class.java)

        authApi.register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        CustomToast.success(this@RegisterActivity, "Registration Successful!")
                    }
                    finish()
                } else {
                    val error = response.errorBody()?.string()
                    runOnUiThread {
                        CustomToast.error(this@RegisterActivity, "Failed: $error")
                    }
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                runOnUiThread {
                    CustomToast.error(this@RegisterActivity, "Error: ${t.message}")
                }
            }
        })
    }
}