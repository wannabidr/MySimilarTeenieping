package com.sss.mysimilarteenieping.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.domain.usecase.GetAnalysisHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainUiState {
    object Loading : MainUiState
    data class Success(val history: List<AnalysisResult>) : MainUiState
    data class Error(val message: String) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAnalysisHistoryUseCase: GetAnalysisHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            getAnalysisHistoryUseCase()
                .onStart { _uiState.value = MainUiState.Loading }
                .catch { e -> _uiState.value = MainUiState.Error(e.message ?: "An unknown error occurred") }
                .collect { historyList ->
                    _uiState.value = MainUiState.Success(historyList)
                }
        }
    }
} 