package com.sss.mysimilarteenieping.data.remote

import com.sss.mysimilarteenieping.data.model.ChatGptRequest
import com.sss.mysimilarteenieping.data.model.ChatGptResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatGptApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatGptRequest
    ): Response<ChatGptResponse>
} 