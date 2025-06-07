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
import android.util.Log

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

    companion object {
        private const val TAG = "ResultViewModel"
    }

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
                    Log.d(TAG, "Analysis result loaded: ${analysisResult.id}")
                    Log.d(TAG, "Shopping links in result: ${analysisResult.shoppingLinks.size}")
                    analysisResult.shoppingLinks.forEachIndexed { index, link ->
                        Log.d(TAG, "Saved shopping link $index: ${link.itemName}")
                    }
                    
                    _uiState.value = ResultUiState.Success(analysisResult)
                    
                    // AnalysisResult에 이미 쇼핑 링크가 있으면 사용하고, 없으면 새로 가져오기
                    if (analysisResult.shoppingLinks.isNotEmpty()) {
                        Log.d(TAG, "Using saved shopping links from AnalysisResult: ${analysisResult.shoppingLinks.size} links")
                        analysisResult.shoppingLinks.forEachIndexed { index, link ->
                            Log.d(TAG, "Saved shopping link $index: ${link.itemName} -> ${link.linkUrl}")
                            Log.d(TAG, "Saved shopping image $index: ${link.itemImageUrl}")
                            Log.d(TAG, "Saved shopping store $index: ${link.storeName}")
                        }
                        _shoppingLinksState.value = analysisResult.shoppingLinks
                        _shoppingLoadingState.value = false
                        Log.d(TAG, "Shopping links state updated with ${analysisResult.shoppingLinks.size} items")
                    } else {
                        Log.d(TAG, "No shopping links in AnalysisResult, fetching new ones")
                        fetchShoppingInfo(analysisResult.similarTeenieping.name)
                    }
                } else {
                    Log.e(TAG, "Analysis result not found for ID: $resultId")
                    _uiState.value = ResultUiState.Error("분석 결과를 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading result details", e)
                _uiState.value = ResultUiState.Error(e.message ?: "결과를 불러오는 중 오류가 발생했습니다.")
            }
        }
    }

    private fun fetchShoppingInfo(teeniepingName: String) {
        Log.d(TAG, "Fetching shopping info for: $teeniepingName")
        viewModelScope.launch {
            _shoppingLoadingState.value = true
            getShoppingInfoUseCase(teeniepingName)
                .catch { e ->
                    Log.e(TAG, "Error fetching shopping info", e)
                    _shoppingLinksState.value = emptyList()
                    _shoppingLoadingState.value = false
                    // TODO: 사용자에게 쇼핑 정보 로드 실패 알림 (예: Toast 메시지)
                }
                .collect { links ->
                    Log.d(TAG, "Fetched ${links.size} shopping links in ResultViewModel")
                    links.forEachIndexed { index, link ->
                        Log.d(TAG, "Fetched shopping link $index: ${link.itemName}")
                    }
                    _shoppingLinksState.value = links
                    _shoppingLoadingState.value = false
                }
        }
    }
} 