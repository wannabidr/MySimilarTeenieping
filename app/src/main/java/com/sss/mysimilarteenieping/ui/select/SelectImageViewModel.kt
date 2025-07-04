package com.sss.mysimilarteenieping.ui.select

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import kotlinx.coroutines.flow.first
import com.sss.mysimilarteenieping.domain.usecase.GetChatGptDescriptionUseCase
import com.sss.mysimilarteenieping.domain.usecase.GetChatGptImageComparisonUseCase
import com.sss.mysimilarteenieping.domain.usecase.GetSimilarTeeniepingUseCase
import com.sss.mysimilarteenieping.domain.usecase.SaveAnalysisResultUseCase
import com.sss.mysimilarteenieping.domain.usecase.GetShoppingInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import android.util.Log

private const val TAG = "SelectImageViewModel"

sealed interface SelectImageUiState {
    object Idle : SelectImageUiState 
    data class ImageSelected(val imageUri: Uri, val imageBitmap: Bitmap) : SelectImageUiState 
    object Analyzing : SelectImageUiState 
    data class AnalysisSuccess(val analysisResultId: String) : SelectImageUiState 
    data class AnalysisError(val message: String) : SelectImageUiState
}

@HiltViewModel
class SelectImageViewModel @Inject constructor(
    private val getSimilarTeeniepingUseCase: GetSimilarTeeniepingUseCase,
    private val saveAnalysisResultUseCase: SaveAnalysisResultUseCase,
    private val getChatGptDescriptionUseCase: GetChatGptDescriptionUseCase,
    private val getChatGptImageComparisonUseCase: GetChatGptImageComparisonUseCase,
    private val getShoppingInfoUseCase: GetShoppingInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SelectImageUiState>(SelectImageUiState.Idle)
    val uiState: StateFlow<SelectImageUiState> = _uiState.asStateFlow()

    private var currentImageUri: Uri? = null
    private var currentImageBitmap: Bitmap? = null

    fun onImageSelected(uri: Uri, bitmap: Bitmap) {
        currentImageUri = uri
        currentImageBitmap = bitmap
        _uiState.value = SelectImageUiState.ImageSelected(uri, bitmap)
    }

    fun startAnalysis() {
        val uri = currentImageUri
        val bitmap = currentImageBitmap

        if (uri == null || bitmap == null) {
            _uiState.value = SelectImageUiState.AnalysisError("이미지가 선택되지 않았습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = SelectImageUiState.Analyzing
            try {
                val (similarTeenieping, similarityScore) = getSimilarTeeniepingUseCase(bitmap)

                if (similarTeenieping == null) {
                    _uiState.value = SelectImageUiState.AnalysisError("닮은 티니핑을 찾지 못했습니다.")
                    return@launch
                }
                Log.d(TAG, "Teenieping classified: ${similarTeenieping.name}, Score: $similarityScore")

                var enhancedTeenieping = similarTeenieping ?: return@launch
                try {
                    Log.d(TAG, "Fetching ChatGPT description for: ${similarTeenieping.name}")
                    val descriptionResult = getChatGptDescriptionUseCase(similarTeenieping.name)
                    if (descriptionResult.isSuccess) {
                        val chatGptDescription = descriptionResult.getOrNull()
                        Log.d(TAG, "ChatGPT description fetched: $chatGptDescription")

                        
                        enhancedTeenieping = similarTeenieping.copy(
                            description = similarTeenieping.description, 
                            details = chatGptDescription 
                        )
                        Log.d(TAG, "Enhanced teenieping - description: ${enhancedTeenieping.description}")
                        Log.d(TAG, "Enhanced teenieping - details: ${enhancedTeenieping.details}")
                    } else {
                        Log.w(TAG, "Failed to fetch ChatGPT description: ${descriptionResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during ChatGPT description fetching", e)
                }

                var chatGptImageComparison = ""
                try {
                    Log.d(TAG, "Fetching ChatGPT image comparison for: ${similarTeenieping.name}")
                    val comparisonResult = getChatGptImageComparisonUseCase(bitmap, enhancedTeenieping)
                    if (comparisonResult.isSuccess) {
                        chatGptImageComparison = comparisonResult.getOrNull() ?: ""
                        Log.d(TAG, "ChatGPT image comparison fetched: $chatGptImageComparison")
                    } else {
                        Log.w(TAG, "Failed to fetch ChatGPT image comparison: ${comparisonResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during ChatGPT image comparison", e)
                }

                val shoppingLinks = try {
                    Log.d(TAG, "Fetching shopping links for: ${similarTeenieping.name}")
                    val links = getShoppingInfoUseCase(similarTeenieping.name).first()
                    Log.d(TAG, "Successfully fetched ${links.size} shopping links from UseCase")
                    links.forEachIndexed { index, link ->
                        Log.d(TAG, "Received shopping link $index: ${link.itemName} -> ${link.linkUrl}")
                        Log.d(TAG, "Received shopping image $index: ${link.itemImageUrl}")
                        Log.d(TAG, "Received shopping store $index: ${link.storeName}")
                    }
                    links
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch shopping links", e)
                    Log.e(TAG, "Exception details: ${e.message}")
                    Log.e(TAG, "Exception stackTrace: ${e.stackTrace.contentToString()}")
                    listOf()
                }

                val finalShoppingLinks = if (shoppingLinks.isEmpty()) {
                    Log.w(TAG, "No shopping links found from API/Repository, creating fallback dummy links")
                    Log.w(TAG, "Original shoppingLinks size was: ${shoppingLinks.size}")
                    listOf()
                } else {
                    Log.d(TAG, "Using real shopping links from API: ${shoppingLinks.size} items")
                    shoppingLinks
                }

                Log.d(TAG, "Final shopping links count: ${finalShoppingLinks.size} items")

                val userImage = UserImage(
                    localFilePath = uri.toString(),
                    fbFilePath = "",
                    createdAt = Date().time
                )
                val analysisResult = AnalysisResult(
                    userImage = userImage,
                    similarTeenieping = enhancedTeenieping, 
                    similarityScore = similarityScore,
                    analysisTimestamp = Date().time,
                    shoppingLinks = finalShoppingLinks, 
                    chatGptDescription = chatGptImageComparison 
                )
                Log.d(TAG, "AnalysisResult created with ${finalShoppingLinks.size} shopping links")
                finalShoppingLinks.forEachIndexed { index, link ->
                    Log.d(TAG, "AnalysisResult shopping link $index: ${link.itemName} -> ${link.linkUrl}")
                    Log.d(TAG, "AnalysisResult shopping image $index: ${link.itemImageUrl}")
                    Log.d(TAG, "AnalysisResult shopping store $index: ${link.storeName}")
                }

                val saveResult = saveAnalysisResultUseCase(uri, analysisResult)

                if (saveResult.isSuccess) {
                    val savedDocumentId = saveResult.getOrThrow()
                    Log.d(TAG, "AnalysisResult saved with ID: $savedDocumentId")
                    _uiState.value = SelectImageUiState.AnalysisSuccess(savedDocumentId)
                } else {
                    Log.e(TAG, "Failed to save analysis result: ${saveResult.exceptionOrNull()?.message}")
                    _uiState.value = SelectImageUiState.AnalysisError(saveResult.exceptionOrNull()?.message ?: "결과 저장에 실패했습니다.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during analysis process", e)
                _uiState.value = SelectImageUiState.AnalysisError(e.message ?: "분석 중 오류가 발생했습니다.")
            }
        }
    }
}