package com.sss.mysimilarteenieping.data.repository

import com.sss.mysimilarteenieping.data.model.ShoppingLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    // TODO: 추후 NaverShoppingApiService 주입 (Retrofit 인터페이스)
) : ShoppingRepository {

    override fun getShoppingInfo(query: String): Flow<List<ShoppingLink>> = flow {
        // TODO: 네이버 쇼핑 API 호출 로직 구현
        // 1. NaverShoppingApiService 를 사용하여 API 호출 (query 사용)
        // 2. API 응답 파싱 (성공/실패 처리)
        // 3. 응답 데이터를 List<ShoppingLink> 형태로 변환
        // 4. emit()으로 결과 방출

        // 예시: 더미 데이터 반환 (실제 구현 시 API 호출로 대체)
        kotlinx.coroutines.delay(1000) // 네트워크 지연 시뮬레이션
        val dummyLinks = listOf(
            ShoppingLink(
                itemName = "[네이버쇼핑] ${query} 인형 최저가",
                linkUrl = "https://search.shopping.naver.com/search/all?query=${query}",
                itemImageUrl = "", // 필요시 실제 이미지 URL 추가
                storeName = "네이버 쇼핑"
            ),
            ShoppingLink(
                itemName = "[네이버쇼핑] ${query} 장난감 특가",
                linkUrl = "https://search.shopping.naver.com/search/all?query=${query}",
                itemImageUrl = "", // 필요시 실제 이미지 URL 추가
                storeName = "네이버 쇼핑"
            )
        )
        emit(dummyLinks)
    }
} 