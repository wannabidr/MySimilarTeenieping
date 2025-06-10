package com.sss.mysimilarteenieping.domain.usecase

import android.net.Uri
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.repository.HistoryRepository
import javax.inject.Inject

class SaveAnalysisResultUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(localImageUri: Uri, analysisResultToSave: AnalysisResult): Result<String> {
        return historyRepository.saveAnalysisResult(localImageUri, analysisResultToSave)
    }
} 