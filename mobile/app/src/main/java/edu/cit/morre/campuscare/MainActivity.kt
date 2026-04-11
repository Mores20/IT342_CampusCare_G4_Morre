package edu.cit.morre.campuscare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.auth.RegisterActivity
import edu.cit.morre.campuscare.auth.RetrofitClient
import edu.cit.morre.campuscare.model.AuthResponse
import edu.cit.morre.campuscare.model.LoginRequest
import edu.cit.morre.campuscare.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val registerLink = findViewById<TextView>(R.id.tvGoToRegister)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val authApi = RetrofitClient.instance.create(AuthApi::class.java)
            authApi.login(LoginRequest(email, password))
                .enqueue(object : Callback<AuthResponse> {

                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (response.isSuccessful) {
                            val token = response.body()?.accessToken
                            if (token != null) {
                                // Save token and email
                                TokenManager.saveToken(this@MainActivity, token)
                                TokenManager.saveEmail(this@MainActivity, email)

                                // Dismiss keyboard
                                val imm = getSystemService(Context.INPUT_METHOD_SERVICE)
                                        as android.view.inputmethod.InputMethodManager
                                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                                // ✅ Navigate to Dashboard
                                val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        } else {
                            val error = response.errorBody()?.string()
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Login Failed: $error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                })
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}