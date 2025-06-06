package com.sss.mysimilarteenieping.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.repository.HistoryRepository
import com.sss.mysimilarteenieping.domain.usecase.GetShoppingInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ResultUiState {
    object Loading : ResultUiState
    data class Success(val result: AnalysisResult) : ResultUiState
    data class Error(val message: String) : ResultUiState
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val getShoppingInfoUseCase: GetShoppingInfoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _shoppingLinksState = MutableStateFlow<List<ShoppingLink>>(emptyList())
    val shoppingLinksState: StateFlow<List<ShoppingLink>> = _shoppingLinksState.asStateFlow()

    private val _shoppingLoadingState = MutableStateFlow<Boolean>(false)
    val shoppingLoadingState: StateFlow<Boolean> = _shoppingLoadingState.asStateFlow()

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
                    _uiState.value = ResultUiState.Success(analysisResult)
                    fetchShoppingInfo(analysisResult.similarTeenieping.name)
                } else {
                    _uiState.value = ResultUiState.Error("분석 결과를 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = ResultUiState.Error(e.message ?: "결과를 불러오는 중 오류가 발생했습니다.")
            }
        }
    }

    private fun fetchShoppingInfo(teeniepingName: String) {
        viewModelScope.launch {
            _shoppingLoadingState.value = true
            getShoppingInfoUseCase(teeniepingName)
                .catch { e ->
                    _shoppingLinksState.value = emptyList()
                    _shoppingLoadingState.value = false
                    // TODO: 사용자에게 쇼핑 정보 로드 실패 알림 (예: Toast 메시지)
                }
                .collect { links ->
                    _shoppingLinksState.value = links
                    _shoppingLoadingState.value = false
                }
        }
    }
} 