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

class GetSimilarTeeniepingUseCase @Inject constructor(
    private val teeniepingClassifier: TeeniepingClassifier,
    private val teeniepingRepository: TeeniepingRepository
) {
    suspend operator fun invoke(userBitmap: Bitmap): Pair<TeeniepingInfo?, Float> = withContext(Dispatchers.IO) {
        Log.d(TAG, "invoke: classification started")

        return@withContext try {
            val classificationResult = teeniepingClassifier.classify(userBitmap)
            val teeniepingInfo = classificationResult.first
            val similarity = classificationResult.second

            Log.d(TAG, "invoke: classification result - TeeniepingInfo: ${teeniepingInfo?.name}, Similarity: $similarity, Index: ${teeniepingInfo?.id}")

            if (teeniepingInfo != null) {
                try {
                    val repositoryInfo = teeniepingRepository.getTeeniepingById(teeniepingInfo.id)
                    val enhancedInfo = repositoryInfo?.let { repoInfo ->
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