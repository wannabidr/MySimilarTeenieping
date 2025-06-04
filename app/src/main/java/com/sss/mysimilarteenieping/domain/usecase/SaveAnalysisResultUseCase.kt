package com.sss.mysimilarteenieping.domain.usecase

import android.net.Uri
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.repository.HistoryRepository
import javax.inject.Inject

/**
 * 분석 결과를 저장하는 유스케이스
 */
class SaveAnalysisResultUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    /**
     * @param localImageUri 사용자가 선택한 이미지의 로컬 Uri
     * @param analysisResultToSave 저장할 분석 결과 데이터 (UserImage 정보는 Repository에서 채워짐)
     * @return 성공 시 저장된 Firestore 문서 ID를 담은 Result, 실패 시 에러 Result
     */
    suspend operator fun invoke(localImageUri: Uri, analysisResultToSave: AnalysisResult): Result<String> {
        return historyRepository.saveAnalysisResult(localImageUri, analysisResultToSave)
    }
} 