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

                // 2. ChatGPT로 설명 가져오기 (선택적)
                var chatGptDescription: String? = null
                try {
                    Log.d(TAG, "Fetching ChatGPT description for: ${similarTeenieping.name}")
                    // 사용자 이미지 특징은 현재 설계에 없으므로 null 전달 또는 필요한 정보 추출 로직 추가
                    val descriptionResult = getChatGptDescriptionUseCase(similarTeenieping.name /*, userImageFeatures = null */)
                    if (descriptionResult.isSuccess) {
                        chatGptDescription = descriptionResult.getOrNull()
                        Log.d(TAG, "ChatGPT description fetched: $chatGptDescription")
                    } else {
                        Log.w(TAG, "Failed to fetch ChatGPT description: ${descriptionResult.exceptionOrNull()?.message}")
                        // 설명 가져오기 실패 시 분석을 중단하지 않고, 설명 없이 진행
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during ChatGPT description fetching", e)
                    // 설명 가져오기 중 예외 발생 시에도 분석은 계속 진행
                }

                // 3. UserImage 및 AnalysisResult 객체 생성
                val userImage = UserImage(
                    localFilePath = uri.toString(),
                    fbFilePath = "",
                    createdAt = Date().time
                )
                val analysisResult = AnalysisResult(
                    userImage = userImage,
                    similarTeenieping = similarTeenieping,
                    similarityScore = similarityScore,
                    analysisTimestamp = Date().time,
                    chatGptDescription = chatGptDescription // 가져온 설명 포함
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