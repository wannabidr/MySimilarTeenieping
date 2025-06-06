package com.sss.mysimilarteenieping

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.sss.mysimilarteenieping.ui.result.ResultViewModel
import com.sss.mysimilarteenieping.ui.result.composables.ResultScreen // 경로 확인 필요
import com.sss.mysimilarteenieping.ui.theme.MySimilarTeeniepingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultActivity : ComponentActivity() {

    private val viewModel: ResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // val resultId = intent.getStringExtra("resultId") // ViewModel에서 SavedStateHandle로 처리

        setContent {
            MySimilarTeeniepingTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val uiState by viewModel.uiState.collectAsState()
                    val shoppingLinks by viewModel.shoppingLinksState.collectAsState()
                    val isShoppingLoading by viewModel.shoppingLoadingState.collectAsState()

                    ResultScreen(
                        uiState = uiState,
                        shoppingLinks = shoppingLinks,
                        isShoppingLoading = isShoppingLoading,
                        onShoppingLinkClicked = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            } catch (e: Exception) {
                                // TODO: Handle invalid URL or no browser app (e.g., show a Toast)
                                e.printStackTrace()
                            }
                        },
                        onRetryClick = {
                            viewModel.loadResultDetails() // 에러 시 재시도
                        },
                        onBackClick = { finish() },
                        onShareClick = { resultText -> // TODO: resultText 외에 이미지도 공유하도록 확장
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, resultText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        }
                    )
                }
            }
        }
    }
} 