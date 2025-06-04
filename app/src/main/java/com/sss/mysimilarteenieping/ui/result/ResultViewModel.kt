package com.sss.mysimilarteenieping.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.repository.HistoryRepository // 직접 Firestore 호출 대신 Repository 사용
// import com.sss.mysimilarteenieping.domain.usecase.GetChatGptDescriptionUseCase // 필요시
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ResultUiState {
    object Loading : ResultUiState
    data class Success(val result: AnalysisResult, val shoppingLinks: List<ShoppingLink>) : ResultUiState
    data class Error(val message: String) : ResultUiState
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val historyRepository: HistoryRepository, // 분석 ID로 결과 로드
    // private val getChatGptDescriptionUseCase: GetChatGptDescriptionUseCase, // ChatGPT 설명용
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val resultId: String? = savedStateHandle.get<String>("resultId")

    init {
        loadResultDetails()
    }

    fun loadResultDetails() {
        if (resultId == null) {
            _uiState.value = ResultUiState.Error("결과 ID가 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResultUiState.Loading
            try {
                val analysisResult = historyRepository.getAnalysisResultById(resultId)
                if (analysisResult != null) {
                    // TODO: 실제 쇼핑 링크 생성 로직 추가 (예: 티니핑 이름 기반 검색)
                    val dummyShoppingLinks = listOf(
                        ShoppingLink("${analysisResult.similarTeenieping.name} 인형", null, "네이버 쇼핑", "https://search.shopping.naver.com/search/all?query=${analysisResult.similarTeenieping.name}+인형"),
                        ShoppingLink("${analysisResult.similarTeenieping.name} 스티커", null, "네이버 쇼핑", "https://search.shopping.naver.com/search/all?query=${analysisResult.similarTeenieping.name}+스티커")
                    )
                    _uiState.value = ResultUiState.Success(analysisResult, dummyShoppingLinks)

                    // (선택) ChatGPT로 설명 가져오기
                    // fetchAndLoadChatGptDescription(analysisResult.similarTeenieping.name, analysisResult.userImage.features) // UserImage에 특징 필드 추가 필요

                } else {
                    _uiState.value = ResultUiState.Error("분석 결과를 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = ResultUiState.Error(e.message ?: "결과를 불러오는 중 오류가 발생했습니다.")
            }
        }
    }

    // private fun fetchAndLoadChatGptDescription(teeniepingName: String, userImageFeatures: String) {
    //     viewModelScope.launch {
    //         val currentData = (_uiState.value as? ResultUiState.Success)
    //         if (currentData != null) {
    //             try {
    //                 val descriptionResult = getChatGptDescriptionUseCase(teeniepingName, userImageFeatures)
    //                 if (descriptionResult.isSuccess) {
    //                     val updatedTeeniepingInfo = currentData.result.similarTeenieping.copy(description = descriptionResult.getOrThrow())
    //                     val updatedAnalysisResult = currentData.result.copy(similarTeenieping = updatedTeeniepingInfo)
    //                     _uiState.value = ResultUiState.Success(updatedAnalysisResult, currentData.shoppingLinks)
    //                 } // 오류 처리는 생략 (필요시 추가)
    //             } catch (e: Exception) {
    //                 // 오류 처리
    //             }
    //         }
    //     }
    // }
} 