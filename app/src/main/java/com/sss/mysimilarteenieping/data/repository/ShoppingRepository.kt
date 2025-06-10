package com.sss.mysimilarteenieping.data.repository

import android.util.Log
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.remote.NaverShoppingApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

interface ShoppingRepository {
    fun getShoppingInfo(query: String): Flow<List<ShoppingLink>>
}

class ShoppingRepositoryImpl @Inject constructor(
    private val naverShoppingApiService: NaverShoppingApiService
) : ShoppingRepository {

    companion object {
        private const val TAG = "ShoppingRepository"
    }

    override fun getShoppingInfo(query: String): Flow<List<ShoppingLink>> = flow {
        Log.d(TAG, "Searching shopping info for: $query")
        
        val searchQuery = "$query 티니핑 굿즈"
        Log.d(TAG, "Search query: $searchQuery")
        
        Log.d(TAG, "Calling Naver Shopping API...")
        
        val response = naverShoppingApiService.searchShopping(
            query = searchQuery,
            display = 6,
            start = 1,
            sort = "sim",
            exclude = "used:rental:cbshop"
        )
        
        Log.d(TAG, "API Response: ${response.code()}, Success: ${response.isSuccessful}")
        Log.d(TAG, "Response headers: ${response.headers()}")

        (if (response.isSuccessful) {
            val naverResponse = response.body()
            Log.d(TAG, "API Response body: $naverResponse")
            Log.d(TAG, "API Response is successful: true")

            if (naverResponse != null) {
                Log.d(TAG, "API Response body is not null")
                Log.d(TAG, "Total available items: ${naverResponse.total}")
                Log.d(TAG, "Items in response: ${naverResponse.items.size}")

                val shoppingLinks = naverResponse.items.take(6).mapIndexed { index, item ->
                    Log.d(TAG, "Processing item [$index]: title=${item.title}, link=${item.link}, image=${item.image}, mall=${item.mallName}")

                    val shoppingLink = ShoppingLink(
                        itemName = cleanTitle(item.title),
                        linkUrl = item.link,
                        itemImageUrl = item.image.takeIf { it.isNotEmpty() } ?: "",
                        storeName = item.mallName.ifEmpty { "네이버 쇼핑" }
                    )

                    Log.d(TAG, "Created ShoppingLink [$index]: itemName=${shoppingLink.itemName}, linkUrl=${shoppingLink.linkUrl}, imageUrl=${shoppingLink.itemImageUrl}, storeName=${shoppingLink.storeName}")

                    shoppingLink
                }

                Log.d(TAG, "Successfully processed ${shoppingLinks.size} shopping items")
                shoppingLinks.forEachIndexed { index, link ->
                    Log.d(TAG, "Final ShoppingLink [$index]: ${link.itemName} -> ${link.linkUrl}")
                    Log.d(TAG, "Final image [$index]: ${link.itemImageUrl}")
                }

                if (shoppingLinks.isNotEmpty()) {
                    Log.d(TAG, "Emitting ${shoppingLinks.size} shopping links")
                    emit(shoppingLinks)
                    Log.d(TAG, "Successfully emitted shopping links")
                } else {
                    Log.w(TAG, "API returned empty items, emitting empty list")
                    emit(emptyList())
                }
            } else {
                Log.w(TAG, "API response body is null, emitting empty list")
                emit(emptyList())
            }
        } else {
            Log.w(TAG, "API Response is NOT successful: ${response.code()}")
            val errorBody = response.errorBody()?.string()
            Log.w(TAG, "API call failed: ${response.code()} ${response.message()}")
            Log.w(TAG, "Error body: $errorBody")

            when (response.code()) {
                403 -> Log.e(TAG, "403 Forbidden - API 권한이 없습니다. 네이버 개발자센터에서 검색 API가 활성화되어 있는지 확인하세요.")
                400 -> Log.e(TAG, "400 Bad Request - 요청 파라미터에 오류가 있습니다: $errorBody")
                500 -> Log.e(TAG, "500 Internal Server Error - 네이버 서버 내부 오류")
            }

            emit(emptyList())
        }) as Unit
    }.catch { e: Throwable ->
        Log.e(TAG, "Exception during API call: ${e.message}", e)
        Log.e(TAG, "Exception type: ${e::class.simpleName}")
        emit(emptyList())
    }
    
    private fun cleanTitle(title: String): String {
        return title
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }
}