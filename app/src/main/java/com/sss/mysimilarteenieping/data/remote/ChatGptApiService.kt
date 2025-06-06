package com.sss.mysimilarteenieping.data.remote

import com.sss.mysimilarteenieping.data.model.ChatGptRequest
import com.sss.mysimilarteenieping.data.model.ChatGptResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * ChatGPT API 연동을 위한 Retrofit 서비스 인터페이스
 */
interface ChatGptApiService {
    /**
     * OpenAI ChatGPT API에 채팅 완성 요청을 보냅니다.
     */
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatGptRequest
    ): Response<ChatGptResponse>
} 