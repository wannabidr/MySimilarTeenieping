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
private const val FIREBASE_WRITE_ENABLED = true

class FirebaseService(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val historyCollection = firestore.collection("history")

    private val userImagesFolder = "user_images"

    suspend fun uploadImage(localFileUri: Uri, fileName: String = UUID.randomUUID().toString()): Result<String> {
        if (!FIREBASE_WRITE_ENABLED) {
            Log.d(TAG, "uploadImage: Firebase write is disabled. Skipping image upload.")
            return Result.success("")
        }
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

    suspend fun saveAnalysisResult(analysisResult: AnalysisResult): Result<String> {
        if (!FIREBASE_WRITE_ENABLED) {
            Log.d(TAG, "saveAnalysisResult: Firebase write is disabled. Skipping analysis result save.")
            return Result.success(UUID.randomUUID().toString())
        }
        Log.d(TAG, "saveAnalysisResult: triggered, analysisResult = $analysisResult")
        return try {
            val documentReference = historyCollection.add(analysisResult).await()
            val documentId = documentReference.id
            
            val updatedAnalysisResult = analysisResult.copy(id = documentId)
            historyCollection.document(documentId).set(updatedAnalysisResult).await()
            
            Log.d(TAG, "saveAnalysisResult: saved with ID = $documentId")
            Result.success(documentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun getAnalysisResultById(id: String): Result<AnalysisResult?> {
        Log.d(TAG, "getAnalysisResultById: triggered, id = $id")
        Log.d(TAG, "Collection path: ${historyCollection.path}")
        return try {
            Log.d(TAG, "Attempting to get document...")
            val documentSnapshot = historyCollection.document(id).get().await()
            Log.d(TAG, "Document exists: ${documentSnapshot.exists()}")
            Log.d(TAG, "Document data: ${documentSnapshot.data}")
            
            val result = try {
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

    suspend fun deleteAnalysisResult(id: String): Result<Unit> {
        if (!FIREBASE_WRITE_ENABLED) {
            Log.d(TAG, "deleteAnalysisResult: Firebase write is disabled. Skipping deletion.")
            return Result.success(Unit)
        }
        Log.d(TAG, "deleteAnalysisResult: id = $id")
        return try {
            historyCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseAnalysisResult(data: Map<String, Any>, documentId: String): AnalysisResult? {
        return try {
            Log.d(TAG, "Manual parsing started for document: $documentId")
            
            val userImageMap = data["userImage"] as? Map<String, Any> ?: return null
            val userImage = UserImage(
                localFilePath = userImageMap["localFilePath"] as? String ?: "",
                fbFilePath = userImageMap["fbFilePath"] as? String ?: "",
                createdAt = (userImageMap["createdAt"] as? Number)?.toLong() ?: 0L
            )
            
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
            
            val shoppingLinksData = data["shoppingLinks"] as? List<Map<String, Any>> ?: emptyList()
            val shoppingLinks = shoppingLinksData.map { linkMap ->
                ShoppingLink(
                    itemName = linkMap["itemName"] as? String ?: "",
                    linkUrl = linkMap["linkUrl"] as? String ?: "",
                    itemImageUrl = linkMap["itemImageUrl"] as? String ?: "",
                    storeName = linkMap["storeName"] as? String ?: ""
                )
            }
            
            val analysisResult = AnalysisResult(
                id = documentId,
                userImage = userImage,
                similarTeenieping = teenieping,
                similarityScore = (data["similarityScore"] as? Number)?.toFloat() ?: 0.0f,
                analysisTimestamp = (data["analysisTimestamp"] as? Number)?.toLong() ?: 0L,
                shoppingLinks = shoppingLinks,
                chatGptDescription = data["chatGptDescription"] as? String ?: ""
            )
            
            Log.d(TAG, "Successfully parsed AnalysisResult with ${shoppingLinks.size} shopping links")
            analysisResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in manual parsing", e)
            null
        }
    }
}