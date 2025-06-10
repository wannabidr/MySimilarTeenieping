package com.sss.mysimilarteenieping.data.model

import androidx.annotation.Keep

@Keep
data class UserImage(
    val localFilePath: String = "",
    val fbFilePath: String = "",
    val createdAt: Long = 0L
) {
    constructor() : this(
        localFilePath = "",
        fbFilePath = "",
        createdAt = 0L
    )
} 