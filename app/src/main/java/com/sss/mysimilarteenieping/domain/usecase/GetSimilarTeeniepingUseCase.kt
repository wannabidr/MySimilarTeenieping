package com.sss.mysimilarteenieping.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.repository.TeeniepingRepository
import com.sss.mysimilarteenieping.ml.TeeniepingClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "GetSimilarTeeniepingUseCase"

/**
 * 사용자 이미지를 분석하여 가장 유사한 티니핑 정보를 가져오는 UseCase
 */
class GetSimilarTeeniepingUseCase @Inject constructor(
    private val teeniepingClassifier: TeeniepingClassifier,
    private val teeniepingRepository: TeeniepingRepository
) {
    /**
     * @param userBitmap 사용자가 선택/촬영한 이미지의 Bitmap
     * @return Pair<TeeniepingInfo?, Float> (가장 유사한 티니핑 정보, 유사도 점수).
     *         티니핑을 찾지 못하거나 오류 발생 시 (null, 0.0f) 또는 (null, 유사도) 반환 가능.
     */
    suspend operator fun invoke(userBitmap: Bitmap): Pair<TeeniepingInfo?, Float> = withContext(Dispatchers.IO) {
        Log.d(TAG, "invoke: classification started")

        // 1. TeeniepingClassifier를 사용하여 이미지 분류 (ID와 유사도 받기)
        // TeeniepingClassifier.classify는 Pair<Int?, Float>를 반환한다고 가정합니다.
        val classificationResult = teeniepingClassifier.classify(userBitmap)
        val teeniepingIdFromClassifier: Int? = classificationResult.first
        val similarity: Float = classificationResult.second

        Log.d(TAG, "invoke: classification result - ID (from classifier): $teeniepingIdFromClassifier, Similarity: $similarity")

        var finalTeeniepingInfo: TeeniepingInfo? = null

        if (teeniepingIdFromClassifier != null) {
            // 2. 분류된 ID(Int)를 String으로 변환하여 Repository에서 전체 TeeniepingInfo 조회
            // TeeniepingInfo의 id는 String 타입이므로 변환합니다.
            val idString = teeniepingIdFromClassifier.toString()
            try {
                Log.d(TAG, "invoke: Fetching TeeniepingInfo from repository for ID: $idString")
                finalTeeniepingInfo = teeniepingRepository.getTeeniepingById(idString)
                if (finalTeeniepingInfo == null) {
                    Log.w(TAG, "invoke: TeeniepingInfo not found in repository for ID: $idString")
                }
            } catch (e: Exception) {
                Log.e(TAG, "invoke: Error fetching TeeniepingInfo from repository for ID: $idString", e)
                // 오류 발생 시 TeeniepingInfo는 null로 두고 유사도만 반환할 수 있음
            }
        } else {
            Log.w(TAG, "invoke: Teenieping ID from classifier was null.")
        }

        Log.d(TAG, "invoke: final result - TeeniepingInfo: ${finalTeeniepingInfo?.name}, Similarity: $similarity")
        return@withContext Pair(finalTeeniepingInfo, similarity)
    }
}