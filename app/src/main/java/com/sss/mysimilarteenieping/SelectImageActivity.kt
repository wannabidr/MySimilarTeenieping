package com.sss.mysimilarteenieping

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import com.sss.mysimilarteenieping.ui.select.SelectImageUiState
import com.sss.mysimilarteenieping.ui.select.SelectImageViewModel
import com.sss.mysimilarteenieping.ui.select.composables.SelectImageScreen
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import com.sss.mysimilarteenieping.util.createImageFile
import com.sss.mysimilarteenieping.util.getCorrectlyOrientedBitmap
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SelectImageActivity : ComponentActivity() {

    private val viewModel: SelectImageViewModel by viewModels()
    private var tempImageUri: Uri? = null

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.onImageSelected(uri, bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                getCorrectlyOrientedBitmap(this, uri)?.let { bitmap ->
                    viewModel.onImageSelected(uri, bitmap)
                }
            }
        } else {
            tempImageUri = null
        }
    }

    private fun createImageFileUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = cacheDir
        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            imageFile
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySimilarTeeniepingTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val uiState by viewModel.uiState.collectAsState()

                    SelectImageScreen(
                        uiState = uiState,
                        onGalleryClick = {
                            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onCameraClick = {
                            val uri = createImageFileUri()
                            tempImageUri = uri
                            takePictureLauncher.launch(uri)
                        },
                        onAnalyzeClick = {
                            viewModel.startAnalysis()
                        },
                        onBackClick = { finish() }
                    )

                    LaunchedEffect(uiState) {
                        if (uiState is SelectImageUiState.AnalysisSuccess) {
                            val resultId = (uiState as SelectImageUiState.AnalysisSuccess).analysisResultId
                            val intent = Intent(this@SelectImageActivity, ResultActivity::class.java).apply {
                                putExtra("resultId", resultId)
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
} 