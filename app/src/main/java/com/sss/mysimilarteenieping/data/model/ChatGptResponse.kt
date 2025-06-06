package com.sss.mysimilarteenieping.data.model

import com.google.gson.annotations.SerializedName

/**
 * ChatGPT API 응답을 위한 데이터 모델
 */
data class ChatGptResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("object")
    val objectType: String,
    
    @SerializedName("created")
    val created: Long,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("choices")
    val choices: List<Choice>,
    
    @SerializedName("usage")
    val usage: Usage?
)

/**
 * ChatGPT 응답의 선택지 구조
 */
data class Choice(
    @SerializedName("index")
    val index: Int,
    
    @SerializedName("message")
    val message: ChatMessage,
    
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * ChatGPT API 사용량 정보
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    
    @SerializedName("total_tokens")
    val totalTokens: Int
) 