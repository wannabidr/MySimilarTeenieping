package com.sss.mysimilarteenieping.data.repository

import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 분석 기록 (History) 데이터 처리를 위한 Repository 인터페이스
 */
interface HistoryRepository {
    /**
     * 모든 분석 기록을 가져옵니다.
     */
    fun getAllAnalysisResults(): Flow<List<AnalysisResult>>

    /**
     * 특정 분석 기록을 가져옵니다.
     */
    suspend fun getAnalysisResultById(id: String): AnalysisResult?

    /**
     * 새로운 분석 결과를 저장합니다. 사용자 이미지도 함께 처리할 수 있습니다.
     *
     * @param localImageUri 로컬 이미지 URI. Storage에 업로드 됩니다.
     * @param analysisResultToSave 저장할 분석 결과 객체. userImage 정보는 내부에서 업데이트 될 수 있습니다.
     * @return 성공 시 저장된 AnalysisResult의 ID를 담은 Result, 실패 시 에러 Result
     */
    suspend fun saveAnalysisResult(localImageUri: android.net.Uri, analysisResultToSave: AnalysisResult): Result<String>

    /**
     * 분석 기록을 삭제합니다.
     */
    suspend fun deleteAnalysisResult(id: String): Result<Unit>
}

/**
 * HistoryRepository의 Firebase 기반 구현체
 */
class HistoryRepositoryImpl(private val firebaseService: FirebaseService) : HistoryRepository {
    override fun getAllAnalysisResults(): Flow<List<AnalysisResult>> = flow {
        // firebaseService를 사용하여 Firestore에서 모든 결과 가져오기
        val result = firebaseService.getAllAnalysisResults()
        if (result.isSuccess) {
            emit(result.getOrThrow())
        } else {
            // TODO: Propagate error appropriately or emit empty list
            emit(emptyList())
            // throw result.exceptionOrNull() ?: Exception("Failed to get all analysis results")
        }
    }

    override suspend fun getAnalysisResultById(id: String): AnalysisResult? {
        // firebaseService를 사용하여 Firestore에서 특정 결과 가져오기
        val result = firebaseService.getAnalysisResultById(id)
        return if (result.isSuccess) {
            result.getOrNull()
        } else {
            // TODO: Handle error (e.g., log it, return null or throw)
            null
        }
    }

    override suspend fun saveAnalysisResult(localImageUri: android.net.Uri, analysisResultToSave: AnalysisResult): Result<String> {
        return try {
            // 1. Upload userImage.localFilePath to Firebase Storage, get fbFilePath
            val uploadResult = firebaseService.uploadImage(localImageUri)
            if (uploadResult.isFailure) {
                return Result.failure(uploadResult.exceptionOrNull() ?: Exception("Image upload failed"))
            }
            val fbFilePath = uploadResult.getOrThrow()

            // 2. Update analysisResult.userImage with the new fbFilePath and original local path
            val updatedUserImage = analysisResultToSave.userImage.copy(
                localFilePath = localImageUri.toString(), // Store original local URI string if needed
                fbFilePath = fbFilePath
            )
            val finalResultToSave = analysisResultToSave.copy(userImage = updatedUserImage)

            // 3. Save analysisResult to Firestore
            val saveDbResult = firebaseService.saveAnalysisResult(finalResultToSave)
            if (saveDbResult.isFailure) {
                return Result.failure(saveDbResult.exceptionOrNull() ?: Exception("Failed to save analysis to DB"))
            }
            Result.success(saveDbResult.getOrThrow()) // Return the ID of the saved document
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAnalysisResult(id: String): Result<Unit> {
        // firebaseService를 사용하여 Firestore에서 결과 삭제
        return firebaseService.deleteAnalysisResult(id)
        // Optionally, delete image from Storage if needed
    }
} 