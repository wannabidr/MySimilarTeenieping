package com.sss.mysimilarteenieping.domain.usecase

import android.graphics.Bitmap
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.repository.TeeniepingRepository // For future use
import com.sss.mysimilarteenieping.ml.TeeniepingClassifier // For TFLite model interaction
import javax.inject.Inject

/**
 * 사용자 이미지와 가장 유사한 티니핑을 찾는 유스케이스
 */
class GetSimilarTeeniepingUseCase @Inject constructor(
    private val teeniepingClassifier: TeeniepingClassifier, // Uncommented
    private val teeniepingRepository: TeeniepingRepository // Uncommented, for fetching full TeeniepingInfo if needed
) {
    /**
     * 주어진 비트맵 이미지를 분석하여 가장 닮은 티니핑 정보와 유사도를 반환합니다.
     * @param userBitmap 사용자 이미지 비트맵
     * @return Pair<TeeniepingInfo?, Float> - (닮은 티니핑 정보, 유사도 점수) 또는 (null, 0.0f) if error
     */
    suspend operator fun invoke(userBitmap: Bitmap): Pair<TeeniepingInfo?, Float> {
        // 1. TeeniepingClassifier를 사용하여 이미지 분류 및 유사도 측정
        val classificationResult = teeniepingClassifier.classify(userBitmap)
        val teeniepingInfoFromClassifier = classificationResult.first
        val similarity = classificationResult.second

        // 2. (Optional) 만약 Classifier가 ID만 반환한다면, Repository에서 전체 정보 조회
        // if (teeniepingInfoFromClassifier?.id != null && teeniepingInfoFromClassifier.description.isEmpty()) {
        //     val fullInfo = teeniepingRepository.getTeeniepingById(teeniepingInfoFromClassifier.id)
        //     return Pair(fullInfo, similarity)
        // }

        return Pair(teeniepingInfoFromClassifier, similarity)
    }
} 