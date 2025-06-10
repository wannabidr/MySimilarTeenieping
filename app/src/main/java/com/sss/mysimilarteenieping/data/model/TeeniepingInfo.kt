package com.sss.mysimilarteenieping.data.model

import androidx.annotation.Keep

@Keep
data class TeeniepingInfo(
    var id: Int = -1,
    val name: String = "",
    val description: String = "",
    val imagePath: String = "",
    val details: String? = null
) {
    constructor() : this(
        id = -1,
        name = "",
        description = "",
        imagePath = "",
        details = null
    )
} 