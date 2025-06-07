package com.sss.mysimilarteenieping.data.model

import androidx.annotation.Keep

/**
 * 티니핑 캐릭터의 기본 정보를 나타냅니다.
 */
@Keep
data class TeeniepingInfo(
    var id: Int = -1, // ML 모델이 예측한 인덱스 값 (숫자)
    val name: String = "",          // 티니핑 이름
    val description: String = "",   // 티니핑 특징 설명
    val imagePath: String = "",     // 티니핑 이미지 리소스 경로, assets 내 경로, 또는 Firebase Storage URL
    val details: String? = null // (Optional) 티니핑 관련 추가 정보 (등장 에피소드 등)
) {
    // Firestore 연동을 위한 기본 생성자 (필수)
    constructor() : this(
        id = -1,
        name = "",
        description = "",
        imagePath = "",
        details = null
    )
} 