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
     *         티니핑을 찾지 못하거나 오류 발생 시 (null, 0.0f) 반환.
     */
    suspend operator fun invoke(userBitmap: Bitmap): Pair<TeeniepingInfo?, Float> = withContext(Dispatchers.IO) {
        Log.d(TAG, "invoke: classification started")

        return@withContext try {
            // TeeniepingClassifier.classify는 이제 Pair<TeeniepingInfo?, Float>를 직접 반환
            val classificationResult = teeniepingClassifier.classify(userBitmap)
            val teeniepingInfo = classificationResult.first
            val similarity = classificationResult.second

            Log.d(TAG, "invoke: classification result - TeeniepingInfo: ${teeniepingInfo?.name}, Similarity: $similarity, Index: ${teeniepingInfo?.id}")

            // TeeniepingClassifier에서 이미 완전한 TeeniepingInfo를 반환하므로 추가 Repository 조회 불필요
            // 필요시 Repository에서 추가 정보를 조회할 수 있음 (예: 상세 설명, 쇼핑 링크 등)
            if (teeniepingInfo != null) {
                try {
                    // (선택사항) Repository에서 추가 정보를 조회하여 병합
                    val repositoryInfo = teeniepingRepository.getTeeniepingById(teeniepingInfo.id)
                    val enhancedInfo = repositoryInfo?.let { repoInfo ->
                        // Repository 정보로 Classifier 결과를 보강
                        teeniepingInfo.copy(
                            description = repoInfo.description.ifEmpty { teeniepingInfo.description },
                            details = repoInfo.details ?: teeniepingInfo.details,
                            imagePath = repoInfo.imagePath.ifEmpty { teeniepingInfo.imagePath }
                        )
                    } ?: teeniepingInfo
                    
                    Log.d(TAG, "invoke: enhanced TeeniepingInfo with repository data")
                    Pair(enhancedInfo, similarity)
                } catch (e: Exception) {
                    Log.w(TAG, "invoke: Failed to enhance TeeniepingInfo from repository, using classifier result", e)
                    Pair(teeniepingInfo, similarity)
                }
            } else {
                Log.w(TAG, "invoke: TeeniepingClassifier returned null")
                Pair(null, similarity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "invoke: Error during classification", e)
            Pair(null, 0.0f)
        }
    }
}