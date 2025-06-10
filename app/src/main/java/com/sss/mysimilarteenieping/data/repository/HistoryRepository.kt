package com.sss.mysimilarteenieping.data.repository

import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface HistoryRepository {
    fun getAllAnalysisResults(): Flow<List<AnalysisResult>>

    suspend fun getAnalysisResultById(id: String): AnalysisResult?

    suspend fun saveAnalysisResult(localImageUri: android.net.Uri, analysisResultToSave: AnalysisResult): Result<String>

    suspend fun deleteAnalysisResult(id: String): Result<Unit>
}

class HistoryRepositoryImpl(private val firebaseService: FirebaseService) : HistoryRepository {
    override fun getAllAnalysisResults(): Flow<List<AnalysisResult>> = flow {
        val result = firebaseService.getAllAnalysisResults()
        if (result.isSuccess) {
            emit(result.getOrThrow())
        } else {
            emit(emptyList())
        }
    }

    override suspend fun getAnalysisResultById(id: String): AnalysisResult? {
        val result = firebaseService.getAnalysisResultById(id)
        return if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
    }

    override suspend fun saveAnalysisResult(localImageUri: android.net.Uri, analysisResultToSave: AnalysisResult): Result<String> {
        return try {
            val uploadResult = firebaseService.uploadImage(localImageUri)
            if (uploadResult.isFailure) {
                return Result.failure(uploadResult.exceptionOrNull() ?: Exception("Image upload failed"))
            }
            val fbFilePath = uploadResult.getOrThrow()

            val updatedUserImage = analysisResultToSave.userImage.copy(
                localFilePath = localImageUri.toString(),
                fbFilePath = fbFilePath
            )
            val finalResultToSave = analysisResultToSave.copy(userImage = updatedUserImage)

            val saveDbResult = firebaseService.saveAnalysisResult(finalResultToSave)
            if (saveDbResult.isFailure) {
                return Result.failure(saveDbResult.exceptionOrNull() ?: Exception("Failed to save analysis to DB"))
            }
            Result.success(saveDbResult.getOrThrow())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAnalysisResult(id: String): Result<Unit> {
        return firebaseService.deleteAnalysisResult(id)
    }
} 