package com.sss.mysimilarteenieping.domain.usecase

import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnalysisHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<AnalysisResult>> {
        return historyRepository.getAllAnalysisResults()
    }
} 