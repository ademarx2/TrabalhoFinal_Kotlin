package com.rameda38.trabalhofinal

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.rameda38.trabalhofinal.databinding.ActivityEditBinding
import com.rameda38.trabalhofinal.model.Expense
import com.rameda38.trabalhofinal.utils.NotificationHelper
import com.rameda38.trabalhofinal.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    
    private var expense: Expense? = null
    private var photoUri: Uri? = null
    private var currentImageUrl: String? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                binding.ivExpense.setImageURI(uri)
                uploadImageToFirebase(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            dispatchTakePictureIntent()
        }
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getLastLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ExpenseViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        firebaseAnalytics = Firebase.analytics

        @Suppress("DEPRECATION")
        expense = intent.getSerializableExtra("EXPENSE") as? Expense

        setupUI()
        setupListeners()
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    }

    private fun setupUI() {
        if (expense != null) {
            binding.tvTitle.text = getString(R.string.title_edit_expense)
            binding.etDescription.setText(expense?.description)
            binding.etValue.setText(expense?.value.toString())
            binding.btnDelete.visibility = View.VISIBLE
            
            currentImageUrl = expense?.imageUrl
            currentLatitude = expense?.latitude
            currentLongitude = expense?.longitude

            if (!currentImageUrl.isNullOrEmpty()) {
                Glide.with(this).load(currentImageUrl).into(binding.ivExpense)
            }
            
            updateLocationText()
        } else {
            binding.tvTitle.text = getString(R.string.title_add_expense)
            binding.btnDelete.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.fabTakePhoto.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnGetLocation.setOnClickListener {
            checkLocationPermission()
        }

        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnDelete.setOnClickListener {
            deleteExpense()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }
        photoFile?.also {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            photoUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance("gs://trabalho-final-android-2a320.firebasestorage.app").reference.child("expenses_images/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    currentImageUrl = downloadUri.toString()
                    Toast.makeText(this, "Imagem enviada!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao enviar imagem: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    updateLocationText()
                } else {
                    Toast.makeText(this, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateLocationText() {
        if (currentLatitude != null && currentLongitude != null) {
            binding.tvLocation.text = "Local: $currentLatitude, $currentLongitude"
        }
    }

    private fun saveExpense() {
        val description = binding.etDescription.text.toString()
        val valueStr = binding.etValue.text.toString()

        if (description.isEmpty() || valueStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_error_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val value = valueStr.toDoubleOrNull() ?: 0.0
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val updatedExpense = Expense(
            id = expense?.id ?: "",
            description = description,
            value = value,
            userId = userId,
            imageUrl = currentImageUrl,
            latitude = currentLatitude,
            longitude = currentLongitude
        )

        if (expense == null) {
            viewModel.insert(updatedExpense)
            logAnalyticsEvent("expense_added", description, value)
            NotificationHelper.showExpenseNotification(this, "Nova Despesa", "Você adicionou: $description")
        } else {
            viewModel.update(updatedExpense)
            logAnalyticsEvent("expense_updated", description, value)
        }

        Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun logAnalyticsEvent(eventName: String, description: String, value: Double) {
        val bundle = Bundle().apply {
            putString("description", description)
            putDouble("value", value)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    private fun deleteExpense() {
        expense?.let {
            viewModel.delete(it)
            logAnalyticsEvent("expense_deleted", it.description, it.value)
            finish()
        }
    }
}
