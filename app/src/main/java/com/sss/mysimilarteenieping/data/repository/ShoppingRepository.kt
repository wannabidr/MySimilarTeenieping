package com.sss.mysimilarteenieping.data.repository

import android.util.Log
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.remote.NaverShoppingApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

/**
 * 쇼핑 정보(예: 네이버 쇼핑 API)를 가져오기 위한 Repository 인터페이스
 */
interface ShoppingRepository {
    /**
     * 주어진 검색어로 쇼핑 정보를 검색합니다.
     *
     * @param query 검색어 (예: 티니핑 이름)
     * @return 쇼핑 링크 리스트를 담은 Flow
     */
    fun getShoppingInfo(query: String): Flow<List<ShoppingLink>>
}

class ShoppingRepositoryImpl @Inject constructor(
    private val naverShoppingApiService: NaverShoppingApiService
) : ShoppingRepository {

    companion object {
        private const val TAG = "ShoppingRepository"
    }

    override fun getShoppingInfo(query: String): Flow<List<ShoppingLink>> = flow {
        try {
            Log.d(TAG, "Searching shopping info for: $query")
            
            // 티니핑 관련 검색어로 확장 및 UTF-8 인코딩 확인
            val searchQuery = "$query 티니핑 굿즈"
            Log.d(TAG, "Search query: $searchQuery")
            
            // API 호출 전 헤더 정보 로깅
            Log.d(TAG, "Calling Naver Shopping API...")
            
            val response = naverShoppingApiService.searchShopping(
                query = searchQuery,
                display = 6,
                start = 1,
                sort = "sim",
                exclude = "used:rental:cbshop" // 중고, 렌탈, 해외직구 제외
            )
            
            Log.d(TAG, "API Response: ${response.code()}, Success: ${response.isSuccessful}")
            Log.d(TAG, "Response headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val naverResponse = response.body()
                Log.d(TAG, "API Response body: $naverResponse")
                
                if (naverResponse != null) {
                    Log.d(TAG, "Total available items: ${naverResponse.total}")
                    Log.d(TAG, "Items in response: ${naverResponse.items.size}")
                    
                    val shoppingLinks = naverResponse.items.take(6).mapIndexed { index, item ->
                        Log.d(TAG, "Processing item [$index]: title=${item.title}, link=${item.link}, image=${item.image}, mall=${item.mallName}")
                        
                        ShoppingLink(
                            itemName = cleanTitle(item.title),
                            linkUrl = item.link,
                            itemImageUrl = item.image.takeIf { it.isNotEmpty() } ?: "",
                            storeName = item.mallName.ifEmpty { "네이버 쇼핑" }
                        )
                    }
                    
                    Log.d(TAG, "Successfully processed ${shoppingLinks.size} shopping items")
                    shoppingLinks.forEach { link ->
                        Log.d(TAG, "Final link: ${link.itemName} -> ${link.linkUrl}")
                        Log.d(TAG, "Final image: ${link.itemImageUrl}")
                    }
                    
                    if (shoppingLinks.isNotEmpty()) {
                        emit(shoppingLinks)
                    } else {
                        Log.w(TAG, "API returned empty items, using dummy data")
                        emit(getDummyShoppingLinks(query))
                    }
                } else {
                    Log.w(TAG, "API response body is null, using dummy data")
                    emit(getDummyShoppingLinks(query))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.w(TAG, "API call failed: ${response.code()} ${response.message()}")
                Log.w(TAG, "Error body: $errorBody")
                
                // 특정 에러 코드 처리
                when (response.code()) {
                    403 -> Log.e(TAG, "403 Forbidden - API 권한이 없습니다. 네이버 개발자센터에서 검색 API가 활성화되어 있는지 확인하세요.")
                    400 -> Log.e(TAG, "400 Bad Request - 요청 파라미터에 오류가 있습니다: $errorBody")
                    500 -> Log.e(TAG, "500 Internal Server Error - 네이버 서버 내부 오류")
                }
                
                // 실패 시 더미 데이터 반환
                emit(getDummyShoppingLinks(query))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call: ${e.message}", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            // 에러 시 더미 데이터 반환
            emit(getDummyShoppingLinks(query))
        }
    }
    
    private fun cleanTitle(title: String): String {
        // HTML 태그 제거 및 텍스트 정리
        return title
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }
    
    private fun getDummyShoppingLinks(query: String): List<ShoppingLink> {
        Log.d(TAG, "Generating dummy shopping links for: $query")
        Log.w(TAG, "Using dummy data - API may have failed or returned no results")
        
        // 실제 네이버 쇼핑과 유사한 형태의 더미 데이터 생성
        return listOf(
            ShoppingLink(
                itemName = "$query 티니핑 피규어 세트 (정품)",
                linkUrl = "https://shopping.naver.com/window-products/8394728462", // 실제 형태의 URL
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394728/83947284625.jpg", // 실제 형태의 이미지 URL
                storeName = "티니핑 공식 스토어"
            ),
            ShoppingLink(
                itemName = "$query 티니핑 봉제인형 30cm",
                linkUrl = "https://shopping.naver.com/window-products/8394728463",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394729/83947294625.jpg",
                storeName = "키즈 랜드"
            ),
            ShoppingLink(
                itemName = "$query 티니핑 키링 컬렉션 5종 세트",
                linkUrl = "https://shopping.naver.com/window-products/8394728464",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394730/83947304625.jpg",
                storeName = "캐릭터 월드"
            ),
            ShoppingLink(
                itemName = "$query 티니핑 스티커북 + 스티커 세트",
                linkUrl = "https://shopping.naver.com/window-products/8394728465",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394731/83947314625.jpg",
                storeName = "문구나라"
            ),
            ShoppingLink(
                itemName = "$query 티니핑 캐릭터 백팩 (어린이용)",
                linkUrl = "https://shopping.naver.com/window-products/8394728466",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394732/83947324625.jpg",
                storeName = "베이비 스토어"
            ),
            ShoppingLink(
                itemName = "$query 티니핑 문구용품 세트 (연필, 지우개, 자)",
                linkUrl = "https://shopping.naver.com/window-products/8394728467",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394733/83947334625.jpg",
                storeName = "스마트 문구"
            )
        )
    }
} 