package com.sss.mysimilarteenieping.data.repository

import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.remote.FirebaseService // For potential future use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

/**
 * 티니핑 기본 정보 (assets 또는 Firestore) 데이터 처리를 위한 Repository 인터페이스
 */
interface TeeniepingRepository {
    /**
     * 모든 티니핑 정보를 가져옵니다.
     */
    fun getAllTeeniepings(): Flow<List<TeeniepingInfo>>

    /**
     * 특정 ID의 티니핑 정보를 가져옵니다.
     */
    suspend fun getTeeniepingById(id: Int): TeeniepingInfo?

    /**
     * (관리자용, 선택 사항) 새로운 티니핑 정보를 추가합니다.
     */
    suspend fun addTeenieping(teeniepingInfo: TeeniepingInfo): Result<Unit>
}

/**
 * TeeniepingRepository의 구현체
 * 초기에는 assets/teenieping_data.json 에서 로드하거나, 추후 Firestore에서 관리할 수 있습니다.
 */
class TeeniepingRepositoryImpl(
    private val firebaseService: FirebaseService, // For potential future use with Firestore
    private val assetManager: AssetManager,
    private val gson: Gson
) : TeeniepingRepository {

    private val teeniepingDataFileName = "teenieping_data.json" // 가정된 파일명
    private var cachedTeeniepings: List<TeeniepingInfo>? = null

    override fun getAllTeeniepings(): Flow<List<TeeniepingInfo>> = flow {
        if (cachedTeeniepings != null) {
            emit(cachedTeeniepings!!)
            return@flow
        }
        try {
            val inputStream = assetManager.open(teeniepingDataFileName)
            val reader = InputStreamReader(inputStream)
            val listType = object : TypeToken<List<TeeniepingInfo>>() {}.type
            val teeniepings: List<TeeniepingInfo> = gson.fromJson(reader, listType)
            reader.close()
            cachedTeeniepings = teeniepings
            emit(teeniepings)
        } catch (e: Exception) {
            // TODO: Handle exception (e.g., file not found, parsing error)
            // For now, emit empty list or throw an error
            emit(emptyList())
            // Log.e("TeeniepingRepository", "Error loading teenieping data from assets", e)
        }
    }.flowOn(Dispatchers.IO) // Perform file I/O on IO dispatcher

    override suspend fun getTeeniepingById(id: Int): TeeniepingInfo? = withContext(Dispatchers.IO) {
        if (cachedTeeniepings == null) {
            // Ensure data is loaded if not already cached (e.g. by calling getAllTeeniepings first or loading here)
            // This basic implementation assumes getAllTeeniepings has been called or will be called.
            // A more robust way is to load it here if null.
            try {
                val inputStream = assetManager.open(teeniepingDataFileName)
                val reader = InputStreamReader(inputStream)
                val listType = object : TypeToken<List<TeeniepingInfo>>() {}.type
                val teeniepings: List<TeeniepingInfo> = gson.fromJson(reader, listType)
                reader.close()
                cachedTeeniepings = teeniepings
            } catch (e: Exception) {
                // Log error
                return@withContext null
            }
        }
        return@withContext cachedTeeniepings?.find { it.id == id }
    }

    override suspend fun addTeenieping(teeniepingInfo: TeeniepingInfo): Result<Unit> {
        // TODO: (Optional) Implement adding new teenieping to Firestore using firebaseService
        // This would typically involve: firebaseService.addTeeniepingData(teeniepingInfo)
        // Also, if using caching, the cache should be updated or invalidated.
        return Result.success(Unit) // Placeholder for asset-based or if not implemented
    }
} 