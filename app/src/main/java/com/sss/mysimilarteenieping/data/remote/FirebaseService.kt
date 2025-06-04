package com.sss.mysimilarteenieping.data.remote

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Firebase Firestore 및 Firebase Storage 관련 로직을 통합하거나 분리하여 제공하는 서비스 클래스
 */
class FirebaseService(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    // Firestore Collections
    private val historyCollection = firestore.collection("history")

    // Storage Folders
    private val userImagesFolder = "user_images"

    /**
     * 파일을 Firebase Storage에 업로드하고 다운로드 URL을 반환합니다.
     */
    suspend fun uploadImage(localFileUri: Uri, fileName: String = UUID.randomUUID().toString()): Result<String> {
        return try {
            val storageRef = storage.reference.child("$userImagesFolder/$fileName")
            storageRef.putFile(localFileUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 분석 결과를 Firestore에 저장합니다.
     */
    suspend fun saveAnalysisResult(analysisResult: AnalysisResult): Result<String> {
        return try {
            val documentReference = historyCollection.add(analysisResult).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 모든 분석 결과를 Firestore에서 가져옵니다.
     */
    suspend fun getAllAnalysisResults(): Result<List<AnalysisResult>> {
        return try {
            val querySnapshot = historyCollection.orderBy("analysisTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await()
            val results = querySnapshot.toObjects(AnalysisResult::class.java)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ID로 특정 분석 결과를 Firestore에서 가져옵니다.
     */
    suspend fun getAnalysisResultById(id: String): Result<AnalysisResult?> {
        return try {
            val documentSnapshot = historyCollection.document(id).get().await()
            val result = documentSnapshot.toObject(AnalysisResult::class.java)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ID로 분석 결과를 Firestore에서 삭제합니다.
     */
    suspend fun deleteAnalysisResult(id: String): Result<Unit> {
        return try {
            historyCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // TODO: Add more Firebase related functions as needed (e.g., for TeeniepingInfo if stored in Firestore)
} 