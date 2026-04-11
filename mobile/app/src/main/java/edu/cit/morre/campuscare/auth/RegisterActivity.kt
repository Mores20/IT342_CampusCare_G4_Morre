package edu.cit.morre.campuscare.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.databinding.ActivityRegisterBinding
import edu.cit.morre.campuscare.model.AuthResponse
import edu.cit.morre.campuscare.model.RegisterRequest
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
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RegisterRequest(firstName, lastName, email, password)
        val authApi = RetrofitClient.instance.create(AuthApi::class.java)

        authApi.register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_LONG).show()
                    }
                    finish()
                } else {
                    val error = response.errorBody()?.string()
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Failed: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}