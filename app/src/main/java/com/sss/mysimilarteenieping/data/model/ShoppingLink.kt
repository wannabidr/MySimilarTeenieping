package com.sss.mysimilarteenieping.data.model

/**
 * 티니핑 관련 상품 쇼핑 링크 정보를 나타냅니다. (AnalysisResult에 포함되거나 별도 관리)
 */
data class ShoppingLink(
    val itemName: String,          // 상품명
    val itemImageUrl: String? = null, // (Optional) 상품 이미지 URL
    val storeName: String,         // 판매처 (예: "네이버 쇼핑")
    val linkUrl: String            // 상품 페이지 URL
) 