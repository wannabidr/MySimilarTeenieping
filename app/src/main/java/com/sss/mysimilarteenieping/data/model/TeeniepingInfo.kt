package com.sss.mysimilarteenieping.data.model

import androidx.annotation.Keep

/**
 * 티니핑 캐릭터의 기본 정보를 나타냅니다.
 */
@Keep
data class TeeniepingInfo(
    var id: String = "", // Firestore Document ID
    val name: String = "",          // 티니핑 이름
    val description: String = "",   // 티니핑 특징 설명 (필요시 ChatGPT 연동하여 생성)
    val imagePath: String = "",     // 티니핑 이미지 리소스 경로, assets 내 경로, 또는 Firebase Storage URL
    val details: String? = null // (Optional) 티니핑 관련 추가 정보 (등장 에피소드 등)
) 