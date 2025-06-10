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

interface TeeniepingRepository {
    fun getAllTeeniepings(): Flow<List<TeeniepingInfo>>

    suspend fun getTeeniepingById(id: Int): TeeniepingInfo?

    suspend fun addTeenieping(teeniepingInfo: TeeniepingInfo): Result<Unit>
}

class TeeniepingRepositoryImpl(
    private val firebaseService: FirebaseService,
    private val assetManager: AssetManager,
    private val gson: Gson
) : TeeniepingRepository {

    private val teeniepingDataFileName = "teenieping_data.json"
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
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getTeeniepingById(id: Int): TeeniepingInfo? = withContext(Dispatchers.IO) {
        if (cachedTeeniepings == null) {
            try {
                val inputStream = assetManager.open(teeniepingDataFileName)
                val reader = InputStreamReader(inputStream)
                val listType = object : TypeToken<List<TeeniepingInfo>>() {}.type
                val teeniepings: List<TeeniepingInfo> = gson.fromJson(reader, listType)
                reader.close()
                cachedTeeniepings = teeniepings
            } catch (e: Exception) {
                return@withContext null
            }
        }
        return@withContext cachedTeeniepings?.find { it.id == id }
    }

    override suspend fun addTeenieping(teeniepingInfo: TeeniepingInfo): Result<Unit> {
        return Result.success(Unit)
    }
} 