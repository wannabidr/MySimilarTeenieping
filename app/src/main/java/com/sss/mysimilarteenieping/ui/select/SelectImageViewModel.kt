package com.sss.mysimilarteenieping.ui.select

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.domain.usecase.GetChatGptDescriptionUseCase
import com.sss.mysimilarteenieping.domain.usecase.GetSimilarTeeniepingUseCase
import com.sss.mysimilarteenieping.domain.usecase.SaveAnalysisResultUseCase
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
    object Idle : SelectImageUiState // 초기 상태 또는 이미지 선택 대기
    data class ImageSelected(val imageUri: Uri, val imageBitmap: Bitmap) : SelectImageUiState // 이미지 선택 완료
    object Analyzing : SelectImageUiState // 분석 중
    data class AnalysisSuccess(val analysisResultId: String) : SelectImageUiState // 분석 및 저장 성공 (결과 ID 반환)
    data class AnalysisError(val message: String) : SelectImageUiState
}

@HiltViewModel
class SelectImageViewModel @Inject constructor(
    private val getSimilarTeeniepingUseCase: GetSimilarTeeniepingUseCase,
    private val saveAnalysisResultUseCase: SaveAnalysisResultUseCase,
    private val getChatGptDescriptionUseCase: GetChatGptDescriptionUseCase
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
                // 1. ML 모델로 유사 티니핑 분석
                val (similarTeenieping, similarityScore) = getSimilarTeeniepingUseCase(bitmap)

                if (similarTeenieping == null) {
                    _uiState.value = SelectImageUiState.AnalysisError("닮은 티니핑을 찾지 못했습니다.")
                    return@launch
                }
                Log.d(TAG, "Teenieping classified: ${similarTeenieping.name}, Score: $similarityScore")

                // 2. ChatGPT로 설명 가져오기 및 TeeniepingInfo.details에 저장
                var enhancedTeenieping = similarTeenieping
                try {
                    Log.d(TAG, "Fetching ChatGPT description for: ${similarTeenieping.name}")
                    val descriptionResult = getChatGptDescriptionUseCase(similarTeenieping.name)
                    if (descriptionResult.isSuccess) {
                        val chatGptDescription = descriptionResult.getOrNull()
                        Log.d(TAG, "ChatGPT description fetched: $chatGptDescription")
                        
                        // ChatGPT 설명을 details 필드에 저장, description은 원래 값 유지
                        enhancedTeenieping = similarTeenieping.copy(
                            description = similarTeenieping.description, // 원래 설명 유지
                            details = chatGptDescription // ChatGPT 설명은 details에 저장
                        )
                        Log.d(TAG, "Enhanced teenieping - description: ${enhancedTeenieping.description}")
                        Log.d(TAG, "Enhanced teenieping - details: ${enhancedTeenieping.details}")
                    } else {
                        Log.w(TAG, "Failed to fetch ChatGPT description: ${descriptionResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during ChatGPT description fetching", e)
                }

                // 3. UserImage 및 AnalysisResult 객체 생성
                val userImage = UserImage(
                    localFilePath = uri.toString(),
                    fbFilePath = "",
                    createdAt = Date().time
                )
                val analysisResult = AnalysisResult(
                    userImage = userImage,
                    similarTeenieping = enhancedTeenieping, // ChatGPT 설명이 포함된 TeeniepingInfo 사용
                    similarityScore = similarityScore,
                    analysisTimestamp = Date().time
                )
                Log.d(TAG, "AnalysisResult created: $analysisResult")

                // 4. 결과 저장 (Firestore 및 Storage)
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