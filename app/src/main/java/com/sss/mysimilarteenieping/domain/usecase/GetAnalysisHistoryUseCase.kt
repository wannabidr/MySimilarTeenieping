package com.sss.mysimilarteenieping.domain.usecase

import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 분석 기록 목록을 가져오는 유스케이스
 */
class GetAnalysisHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<AnalysisResult>> {
        return historyRepository.getAllAnalysisResults()
    }
} 