package com.rameda38.trabalhofinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rameda38.trabalhofinal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var emailFragment: EmailInputFragment
    private lateinit var passwordFragment: PasswordInputFragment
    private lateinit var loginBtnFragment: ButtonFragment

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.e("LoginActivity", "Erro no Google Sign In", e)
                    Toast.makeText(this, getString(R.string.msg_error_google) + ": ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (auth.currentUser != null) {
            goToHome()
        }

        supportFragmentManager.executePendingTransactions()
        emailFragment = supportFragmentManager.findFragmentById(R.id.fragmentEmail) as EmailInputFragment
        passwordFragment = supportFragmentManager.findFragmentById(R.id.fragmentPassword) as PasswordInputFragment
        loginBtnFragment = supportFragmentManager.findFragmentById(R.id.fragmentBtnLogin) as ButtonFragment

        loginBtnFragment.setButtonText(getString(R.string.btn_login))
        loginBtnFragment.setOnClickListener {
            loginUser()
        }

        binding.btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = emailFragment.getEmail()
            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.hint_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.msg_reset_email_sent), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.msg_reset_email_error), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loginUser() {
        val email = emailFragment.getEmail()
        val password = passwordFragment.getPassword()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_error_fields), Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Log.w("LoginActivity", "Falha no login", task.exception)
                    Toast.makeText(this, "${getString(R.string.msg_error_auth)}: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("user_name", user?.displayName)
                        putString("user_email", user?.email)
                        apply()
                    }
                    goToHome()
                } else {
                    Log.e("LoginActivity", "Erro na autenticação Firebase via Google")
                    Toast.makeText(this, getString(R.string.msg_error_firebase), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
