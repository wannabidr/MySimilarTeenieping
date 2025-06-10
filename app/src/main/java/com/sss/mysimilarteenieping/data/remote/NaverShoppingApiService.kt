package com.sss.mysimilarteenieping.data.remote

import com.sss.mysimilarteenieping.data.model.NaverShoppingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverShoppingApiService {
    
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