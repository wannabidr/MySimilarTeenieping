package com.sss.mysimilarteenieping.data.model

/**
 * 사용자가 선택하거나 촬영한 이미지 정보를 나타냅니다.
 */
data class UserImage(
    val localFilePath: String,      // 로컬 파일 경로
    val fbFilePath: String, // Firebase Storage에 업로드된 이미지 URL
    val createdAt: Long        // 이미지 생성 또는 선택 시각 (타임스탬프)
) 