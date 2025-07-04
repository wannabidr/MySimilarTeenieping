package com.sss.mysimilarteenieping.data.model

import com.google.gson.annotations.SerializedName

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