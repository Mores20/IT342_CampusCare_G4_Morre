package edu.cit.morre.campuscare

import android.content.Intent
import android.content.Context
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

            android.util.Log.d("LOGIN", "Button clicked, email: $email")

            val authApi = RetrofitClient.instance.create(AuthApi::class.java)
            authApi.login(LoginRequest(email, password))
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        android.util.Log.d("LOGIN", "Response code: ${response.code()}")
                        android.util.Log.d("LOGIN", "Body: ${response.body()}")

                        try {
                            if (response.isSuccessful) {
                                val token = response.body()?.accessToken
                                android.util.Log.d("LOGIN", "Token null? ${token == null}")

                                if (token != null) {
                                    android.util.Log.d("LOGIN", "Saving token...")
                                    TokenManager.saveToken(this@MainActivity, token)
                                    android.util.Log.d("LOGIN", "Token saved, showing dialog...")

                                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                                    android.app.AlertDialog.Builder(this@MainActivity)
                                        .setTitle("Success")
                                        .setMessage("Login successful!")
                                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                        .show()

                                    android.util.Log.d("LOGIN", "Dialog shown!")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("LOGIN", "CRASH: ${e.javaClass.simpleName}: ${e.message}")
                            android.util.Log.e("LOGIN", "Stack: ${e.stackTraceToString()}")
                        }
                    }


                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        android.util.Log.e("LOGIN", "Failed: ${t.javaClass.simpleName}: ${t.message}")
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
    // ✅ Nothing else goes here — no overrides outside onCreate
}