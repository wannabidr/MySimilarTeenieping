package com.sss.mysimilarteenieping.data.remote

import com.sss.mysimilarteenieping.data.model.NaverShoppingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 네이버 쇼핑 API를 위한 Retrofit 서비스 인터페이스
 * 참고: https://developers.naver.com/docs/serviceapi/search/shopping/shopping.md
 */
interface NaverShoppingApiService {
    
    /**
     * 네이버 쇼핑 검색 API
     * @param query 검색어 (UTF-8 인코딩 필수)
     * @param display 검색 결과 출력 건수 (1~100, 기본값 10)
     * @param start 검색 시작 위치 (1~1000, 기본값 1)
     * @param sort 정렬 옵션 (sim: 정확도순, date: 날짜순, asc: 가격오름차순, dsc: 가격내림차순)
     * @param filter 검색 결과에 포함할 상품 유형 (naverpay: 네이버페이 연동 상품)
     * @param exclude 검색 결과에서 제외할 상품 유형 (used: 중고, rental: 렌탈, cbshop: 해외직구/구매대행)
     */
    @GET("v1/search/shop.json")
    suspend fun searchShopping(
        @Query("query") query: String,
        @Query("display") display: Int = 6,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim",
        @Query("filter") filter: String? = null,
        @Query("exclude") exclude: String? = null
    ): Response<NaverShoppingResponse>
} 