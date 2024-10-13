package br.com.projetointegrador.keepsafe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import br.com.projetointegrador.keepsafe.databinding.ActivityTakePhotosBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TakePhotosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakePhotosBinding
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private var photoCounter = 0
    private var photosToTake = 0
    private lateinit var cameraProvider: ProcessCameraProvider
    private val imagePaths = ArrayList<String>() // Variável de classe para armazenar os caminhos das imagens

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.onePhotoButton -> {
                    binding.twoPhotosButton.isEnabled = false
                    photosToTake = 1
                }
                R.id.twoPhotosButton -> {
                    binding.onePhotoButton.isEnabled = false
                    photosToTake = 2
                }
            }
            binding.captureButton.visibility = View.VISIBLE
        }

        binding.captureButton.setOnClickListener {
            if (photosToTake > 0 && photoCounter < photosToTake) {
                takePhoto()
            }
        }

        binding.continueButton.setOnClickListener {
            // Lógica para continuar após tirar fotos
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch(exc: Exception) {
                exc.printStackTrace()
                Toast.makeText(this, "Não foi possível iniciar a câmera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                    Toast.makeText(this@TakePhotosActivity, "Erro ao capturar foto", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imagePaths.add(photoFile.absolutePath)
                    photoCounter++
                    Toast.makeText(this@TakePhotosActivity, "Foto $photoCounter salva", Toast.LENGTH_SHORT).show()

                    if (photoCounter == photosToTake) {
                        binding.continueButton.visibility = View.VISIBLE

                        val userId = intent.getStringExtra("userId")
                        val userDocId = intent.getStringExtra("docID")

                        val intent = Intent(this@TakePhotosActivity, PreviewActivity::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("docID",userDocId)
                        intent.putStringArrayListExtra("imagePaths", imagePaths)
                        startActivity(intent)
                    }
                }
            })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
