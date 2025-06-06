package com.sss.mysimilarteenieping.data.model

import androidx.annotation.Keep

/**
 * 이미지 분석 결과를 저장하고 관리합니다. 이 데이터는 Firebase Firestore에 저장됩니다.
 */
@Keep // Proguard 규칙 자동 생성을 위해 필요할 수 있음 (Firebase)
data class AnalysisResult(
    var id: String = "", // Firestore 문서 ID (자동 생성 또는 직접 할당)
    val userImage: UserImage,           // 사용자가 입력한 이미지 정보 (URL 포함)
    val similarTeenieping: TeeniepingInfo, // 가장 닮은 티니핑 정보 (ChatGPT 설명이 description에 포함됨)
    val similarityScore: Float = 0.0f,
    val analysisTimestamp: Long = 0L
) {
    // Firestore 연동을 위한 기본 생성자 (필수)
    constructor() : this(
        id = "",
        userImage = UserImage(localFilePath = "", fbFilePath = "", createdAt = 0L),
        similarTeenieping = TeeniepingInfo(id = "", name = "", description = "", imagePath = ""),
        similarityScore = 0.0f,
        analysisTimestamp = 0L
    )
} 