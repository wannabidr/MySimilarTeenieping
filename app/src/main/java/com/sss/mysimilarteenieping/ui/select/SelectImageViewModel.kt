package com.sss.mysimilarteenieping.ui.select

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.domain.usecase.GetSimilarTeeniepingUseCase
import com.sss.mysimilarteenieping.domain.usecase.SaveAnalysisResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

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
    private val saveAnalysisResultUseCase: SaveAnalysisResultUseCase
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

                // 2. UserImage 및 AnalysisResult 객체 생성
                // UserImage의 fbFilePath는 HistoryRepositoryImpl에서 이미지 업로드 후 설정됨
                val userImage = UserImage(
                    localFilePath = uri.toString(), // 로컬 URI 저장
                    fbFilePath = "", // Repository에서 채워질 예정
                    createdAt = Date().time
                )
                // AnalysisResult는 id 없이 생성 (Firestore에서 자동 생성되거나 Repository에서 설정)
                val analysisResult = AnalysisResult(
                    userImage = userImage,
                    similarTeenieping = similarTeenieping,
                    similarityScore = similarityScore,
                    analysisTimestamp = Date().time
                    // shoppingLinks는 필요시 Repository나 FirebaseService에서 추가 가능, 또는 기본값 emptyList() 사용
                )

                // 3. 결과 저장 (Firestore 및 Storage)
                // SaveAnalysisResultUseCase는 이제 Uri와 AnalysisResult를 받음
                val saveResult = saveAnalysisResultUseCase(uri, analysisResult)

                if (saveResult.isSuccess) {
                    val savedDocumentId = saveResult.getOrThrow() // 성공 시 Firestore 문서 ID를 가져옴
                    _uiState.value = SelectImageUiState.AnalysisSuccess(savedDocumentId)
                } else {
                    _uiState.value = SelectImageUiState.AnalysisError(saveResult.exceptionOrNull()?.message ?: "결과 저장에 실패했습니다.")
                }

            } catch (e: Exception) {
                _uiState.value = SelectImageUiState.AnalysisError(e.message ?: "분석 중 오류가 발생했습니다.")
            }
        }
    }
} 