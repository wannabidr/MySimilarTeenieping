package com.sss.mysimilarteenieping.data.remote

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "FirebaseService"

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
        Log.d(TAG, "uploadImage: localFileUri = $localFileUri, fileName = $fileName")
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
        Log.d(TAG, "saveAnalysisResult: triggered, analysisResult = $analysisResult")
        return try {
            val documentReference = historyCollection.add(analysisResult).await()
            val documentId = documentReference.id
            
            // ID를 포함한 완전한 객체로 업데이트
            val updatedAnalysisResult = analysisResult.copy(id = documentId)
            historyCollection.document(documentId).set(updatedAnalysisResult).await()
            
            Log.d(TAG, "saveAnalysisResult: saved with ID = $documentId")
            Result.success(documentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 모든 분석 결과를 Firestore에서 가져옵니다.
     */
    suspend fun getAllAnalysisResults(): Result<List<AnalysisResult>> {
        Log.d(TAG, "getAllAnalysisResults: triggered")
        return try {
            val querySnapshot = historyCollection.orderBy("analysisTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await()
            val results = querySnapshot.documents.mapNotNull { document ->
                document.data?.let { data ->
                    parseAnalysisResult(data, document.id)
                }
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ID로 특정 분석 결과를 Firestore에서 가져옵니다.
     */
    suspend fun getAnalysisResultById(id: String): Result<AnalysisResult?> {
        Log.d(TAG, "getAnalysisResultById: triggered, id = $id")
        Log.d(TAG, "Collection path: ${historyCollection.path}")
        return try {
            Log.d(TAG, "Attempting to get document...")
            val documentSnapshot = historyCollection.document(id).get().await()
            Log.d(TAG, "Document exists: ${documentSnapshot.exists()}")
            Log.d(TAG, "Document data: ${documentSnapshot.data}")
            
            val result = try {
                // 수동 파싱으로 타입 변환 문제 해결
                val data = documentSnapshot.data
                if (data != null) {
                    parseAnalysisResult(data, documentSnapshot.id)
                } else {
                    null
                }
            } catch (parseException: Exception) {
                Log.e(TAG, "Failed to parse document to AnalysisResult: ${parseException.message}", parseException)
                Log.e(TAG, "Raw document data: ${documentSnapshot.data}")
                null
            }
            Log.d(TAG, "Parsed result: $result")
            
            if (result == null && documentSnapshot.exists()) {
                Log.w(TAG, "Document exists but failed to parse to AnalysisResult")
                Log.w(TAG, "Raw document data: ${documentSnapshot.data}")
                Log.w(TAG, "Document ID: ${documentSnapshot.id}")
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document ID '$id': ${e.message}", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            if (e.cause != null) {
                Log.e(TAG, "Cause: ${e.cause?.message}")
            }
            Result.failure(e)
        }
    }

    /**
     * ID로 분석 결과를 Firestore에서 삭제합니다.
     */
    suspend fun deleteAnalysisResult(id: String): Result<Unit> {
        Log.d(TAG, "deleteAnalysisResult: id = $id")
        return try {
            historyCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Firestore 데이터를 수동으로 AnalysisResult로 파싱합니다.
     * 타입 변환 문제(특히 Int/Long)를 해결하기 위해 사용됩니다.
     */
    private fun parseAnalysisResult(data: Map<String, Any>, documentId: String): AnalysisResult? {
        return try {
            Log.d(TAG, "Manual parsing started for document: $documentId")
            
            // UserImage 파싱
            val userImageMap = data["userImage"] as? Map<String, Any> ?: return null
            val userImage = UserImage(
                localFilePath = userImageMap["localFilePath"] as? String ?: "",
                fbFilePath = userImageMap["fbFilePath"] as? String ?: "",
                createdAt = (userImageMap["createdAt"] as? Number)?.toLong() ?: 0L
            )
            
            // TeeniepingInfo 파싱 (Int/Long 변환 처리)
            val teeniepingMap = data["similarTeenieping"] as? Map<String, Any> ?: return null
            val teeniepingId = when (val idValue = teeniepingMap["id"]) {
                is Number -> idValue.toInt()
                is String -> idValue.toIntOrNull() ?: -1
                else -> -1
            }
            
            val teenieping = TeeniepingInfo(
                id = teeniepingId,
                name = teeniepingMap["name"] as? String ?: "",
                description = teeniepingMap["description"] as? String ?: "",
                imagePath = teeniepingMap["imagePath"] as? String ?: "",
                details = teeniepingMap["details"] as? String
            )
            
            // ShoppingLinks 파싱
            val shoppingLinksData = data["shoppingLinks"] as? List<Map<String, Any>> ?: emptyList()
            val shoppingLinks = shoppingLinksData.map { linkMap ->
                ShoppingLink(
                    itemName = linkMap["itemName"] as? String ?: "",
                    linkUrl = linkMap["linkUrl"] as? String ?: "",
                    itemImageUrl = linkMap["itemImageUrl"] as? String ?: "",
                    storeName = linkMap["storeName"] as? String ?: ""
                )
            }
            
            // AnalysisResult 생성
            val analysisResult = AnalysisResult(
                id = documentId,
                userImage = userImage,
                similarTeenieping = teenieping,
                similarityScore = (data["similarityScore"] as? Number)?.toFloat() ?: 0.0f,
                analysisTimestamp = (data["analysisTimestamp"] as? Number)?.toLong() ?: 0L,
                shoppingLinks = shoppingLinks
            )
            
            Log.d(TAG, "Successfully parsed AnalysisResult with ${shoppingLinks.size} shopping links")
            analysisResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in manual parsing", e)
            null
        }
    }
    
    // TODO: Add more Firebase related functions as needed (e.g., for TeeniepingInfo if stored in Firestore)
} 