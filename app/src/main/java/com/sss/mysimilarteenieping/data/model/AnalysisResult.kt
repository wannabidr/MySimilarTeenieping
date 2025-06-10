package com.sss.mysimilarteenieping.data.model

import androidx.annotation.Keep

@Keep
data class AnalysisResult(
    var id: String = "",
    val userImage: UserImage,
    val similarTeenieping: TeeniepingInfo,
    val similarityScore: Float = 0.0f,
    val analysisTimestamp: Long = 0L,
    val shoppingLinks: List<ShoppingLink> = emptyList(),
    val chatGptDescription: String = ""
) {
    constructor() : this(
        id = "",
        userImage = UserImage(localFilePath = "", fbFilePath = "", createdAt = 0L),
        similarTeenieping = TeeniepingInfo(id = -1, name = "", description = "", imagePath = ""),
        similarityScore = 0.0f,
        analysisTimestamp = 0L,
        shoppingLinks = emptyList(),
        chatGptDescription = ""
    )
} 