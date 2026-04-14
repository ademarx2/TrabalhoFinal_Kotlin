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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.rameda38.trabalhofinal.databinding.ActivityRegisterBinding
import android.net.Uri

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val database: DatabaseReference = FirebaseDatabase.getInstance("https://trabalho-final-android-2a320-default-rtdb.firebaseio.com/").getReference("users")
    private val storage = FirebaseStorage.getInstance("gs://trabalho-final-android-2a320.firebasestorage.app")

    private var profileImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            profileImageUri = uri
            binding.ivProfileCapa.setImageURI(uri)
        }
    }

    private lateinit var emailFragment: EmailInputFragment
    private lateinit var passwordFragment: PasswordInputFragment
    private lateinit var registerBtnFragment: ButtonFragment

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("RegisterActivity", "Erro Google Sign In", e)
                Toast.makeText(this, getString(R.string.msg_error_google) + ": ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        
        supportFragmentManager.executePendingTransactions()
        emailFragment = supportFragmentManager.findFragmentById(R.id.fragmentEmail) as EmailInputFragment
        passwordFragment = supportFragmentManager.findFragmentById(R.id.fragmentPassword) as PasswordInputFragment
        registerBtnFragment = supportFragmentManager.findFragmentById(R.id.fragmentBtnRegister) as ButtonFragment

        registerBtnFragment.setButtonText(getString(R.string.btn_register))
        registerBtnFragment.setOnClickListener {
            registerUser()
        }

        binding.btnGoogleRegister.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.ivProfileCapa.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString()
        val email = emailFragment.getEmail()
        val password = passwordFragment.getPassword()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_error_fields), Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        uploadCapaAndSaveUser(userId, name, email)
                    }
                } else {
                    Log.e("RegisterActivity", "Falha no registro", task.exception)
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
                    val userId = user?.uid
                    if (userId != null) {
                        uploadCapaAndSaveUser(userId, user.displayName ?: "User", user.email ?: "")
                    }
                } else {
                    Log.e("RegisterActivity", "Erro auth Google no Registro")
                    Toast.makeText(this, getString(R.string.msg_error_firebase), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadCapaAndSaveUser(userId: String, name: String, email: String) {
        val uri = profileImageUri
        if (uri != null) {
            val ref = storage.reference.child("capas/$userId.jpg")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveUserToDatabase(userId, name, email, downloadUri.toString())
                }
            }.addOnFailureListener {
                saveUserToDatabase(userId, name, email, null)
            }
        } else {
            saveUserToDatabase(userId, name, email, null)
        }
    }

    private fun saveUserToDatabase(userId: String, name: String, email: String, capaUrl: String? = null) {
        val userProfile = mutableMapOf("name" to name, "email" to email)
        capaUrl?.let { userProfile["capaUrl"] = it }
        
        database.child(userId).setValue(userProfile).addOnCompleteListener {
             Toast.makeText(this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show()
             goToHome()
        }
        
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_capa", capaUrl)
            apply()
        }
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}
