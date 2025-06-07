package com.sss.mysimilarteenieping.data.model

import com.google.gson.annotations.SerializedName

/**
 * ChatGPT API 요청을 위한 데이터 모델
 */
data class ChatGptRequest(
    @SerializedName("model")
    val model: String = "gpt-3.5-turbo",
    
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    
    @SerializedName("max_tokens")
    val maxTokens: Int = 150,
    
    @SerializedName("temperature")
    val temperature: Double = 0.7
)

/**
 * ChatGPT 메시지 구조
 */
data class ChatMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    
    @SerializedName("content")
    val content: String
) 