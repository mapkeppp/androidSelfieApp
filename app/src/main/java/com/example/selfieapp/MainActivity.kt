package com.example.selfieapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.selfieapp.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var photoUri: Uri? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.imageView.setImageURI(photoUri)
            Toast.makeText(this, "Фото успішно збережено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Помилка при збереженні фото", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Необхідний дозвіл на використання камери", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.takeSelfieButton.setOnClickListener {
            checkCameraPermission()
        }

        binding.sendSelfieButton.setOnClickListener {
            if (photoUri != null) {
                sendEmail()
            } else {
                Toast.makeText(this, "Спочатку зробіть фото", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Потрібен дозвіл на використання камери", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                photoFile
            )
            takePhotoLauncher.launch(photoUri)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(this, "Помилка при створенні фото: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("SELFIE_${timeStamp}_", ".jpg", storageDir)
    }

    private fun sendEmail() {
        photoUri?.let { uri ->
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
                    putExtra(Intent.EXTRA_SUBJECT, "DigiJED [Ваше ім'я та прізвище]")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, """
                        Добрий день!
                        
                        Прикріплюю селфі для завдання.
                        
                        Посилання на репозиторій: https://github.com/your-repo
                    """.trimIndent())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Виберіть email клієнт"))
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(this, "Помилка при відправці email: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}