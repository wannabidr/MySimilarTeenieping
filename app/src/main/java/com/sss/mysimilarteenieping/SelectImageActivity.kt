package com.sss.mysimilarteenieping

import android.content.Intent
import android.graphics.BitmapFactory
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
import com.sss.mysimilarteenieping.ui.select.SelectImageUiState
import com.sss.mysimilarteenieping.ui.select.SelectImageViewModel
import com.sss.mysimilarteenieping.ui.select.composables.SelectImageScreen // 경로 확인 필요
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.InputStream

@AndroidEntryPoint
class SelectImageActivity : ComponentActivity() {

    private val viewModel: SelectImageViewModel by viewModels()

    // Photo Picker 결과 처리
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
                // TODO: Handle exception (e.g., show error message)
                e.printStackTrace()
            }
        } else {
            // No media selected
        }
    }

    // TODO: 카메라 결과 처리 로직 추가 (ActivityResultContracts.TakePicture())

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
                            // TODO: Implement camera launch
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
                            finish() // 현재 액티비티 종료
                        }
                        // TODO: Handle AnalysisError state (e.g., show a Toast or Snackbar)
                    }
                }
            }
        }
    }
} 