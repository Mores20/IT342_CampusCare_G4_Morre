package edu.cit.morre.campuscare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import edu.cit.morre.campuscare.api.AuthApi
import edu.cit.morre.campuscare.auth.AdminDashboardActivity
import edu.cit.morre.campuscare.auth.RegisterActivity
import edu.cit.morre.campuscare.auth.RetrofitClient
import edu.cit.morre.campuscare.auth.StudentDashboardActivity
import edu.cit.morre.campuscare.model.AuthResponse
import edu.cit.morre.campuscare.model.LoginRequest
import edu.cit.morre.campuscare.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    // ✅ Google Sign-In result handler
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                sendGoogleTokenToBackend(idToken)
            } else {
                Toast.makeText(this, "Google Sign-In failed: No token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Auto-login if token exists
        val existingToken = TokenManager.getToken(this)
        val existingRole = TokenManager.getRole(this)
        if (existingToken != null && existingRole != null) {
            navigateByRole(existingRole)
            return
        }

        // ✅ Setup Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val registerLink = findViewById<TextView>(R.id.tvGoToRegister)
        val googleBtn = findViewById<Button>(R.id.googleBtn)

        // ✅ Normal Login
        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false
            loginBtn.text = "Logging in..."

            val authApi = RetrofitClient.instance.create(AuthApi::class.java)
            authApi.login(LoginRequest(email, password))
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        loginBtn.isEnabled = true
                        loginBtn.text = "Login"

                        if (response.isSuccessful) {
                            handleAuthResponse(response.body())
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity,
                                    "Login Failed: ${response.errorBody()?.string()}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        loginBtn.isEnabled = true
                        loginBtn.text = "Login"
                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                })
        }

        // ✅ Google Sign-In button
        googleBtn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun sendGoogleTokenToBackend(idToken: String) {
        val authApi = RetrofitClient.instance.create(AuthApi::class.java)
        authApi.googleLogin(mapOf("token" to idToken))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        handleAuthResponse(response.body())
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "Google login failed: ${response.errorBody()?.string()}",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "Google login error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }
    private fun signOutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso).signOut()
    }

    private fun handleAuthResponse(body: AuthResponse?) {
        val token = body?.accessToken ?: return
        TokenManager.saveToken(this, token)
        body.refreshToken?.let { TokenManager.saveRefreshToken(this, it) }
        body.email?.let { TokenManager.saveEmail(this, it) }
        body.firstName?.let { TokenManager.saveFirstName(this, it) }
        body.lastName?.let { TokenManager.saveLastName(this, it) }
        body.role?.let { TokenManager.saveRole(this, it) }

        // Dismiss keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        navigateByRole(body.role ?: "STUDENT")
    }

    private fun navigateByRole(role: String) {
        val intent = if (role == "ADMIN") {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, StudentDashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}