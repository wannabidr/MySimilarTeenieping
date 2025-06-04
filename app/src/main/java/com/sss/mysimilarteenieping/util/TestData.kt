package com.sss.mysimilarteenieping.util

import com.sss.mysimilarteenieping.data.model.AnalysisResult
import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.model.UserImage
import java.util.Date // Added for Date() constructor

object TestData {

    // For MainScreen Preview
    val dummyUserImage1 = UserImage(
        localFilePath = "/path/to/user_image1.jpg",
        fbFilePath = "https://firebasestorage.googleapis.com/v0/b/app.appspot.com/o/user_images%2Fimage1.jpg?alt=media",
        createdAt = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000 // 2 days ago
    )

    val dummyTeenieping1 = TeeniepingInfo(
        id = "TP001",
        name = "방글핑",
        description = "언제나 방긋 웃는 행복의 티니핑!",
        imagePath = "https://via.placeholder.com/150/FFAACC/000000?Text=Banggleping"
    )

    val dummyResult1 = AnalysisResult(
        id = "result001",
        userImage = dummyUserImage1,
        similarTeenieping = dummyTeenieping1,
        similarityScore = 0.85f,
        analysisTimestamp = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
        shoppingLinks = listOf(
            ShoppingLink(itemName = "방글핑 인형", linkUrl = "https://example.com/banggleping_doll", storeName = "티니핑 스토어")
        )
    )

    val dummyUserImage2 = UserImage(
        localFilePath = "/path/to/user_image2.jpg",
        fbFilePath = "https://firebasestorage.googleapis.com/v0/b/app.appspot.com/o/user_images%2Fimage2.jpg?alt=media",
        createdAt = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000 // 1 day ago
    )

    val dummyTeenieping2 = TeeniepingInfo(
        id = "TP002",
        name = "믿어핑",
        description = "굳게 믿는 마음의 티니핑!",
        imagePath = "https://via.placeholder.com/150/AACCFF/000000?Text=Mideoping"
    )

    val dummyResult2 = AnalysisResult(
        id = "result002",
        userImage = dummyUserImage2,
        similarTeenieping = dummyTeenieping2,
        similarityScore = 0.92f,
        analysisTimestamp = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000,
        shoppingLinks = emptyList()
    )

    val previewHistoryList: List<AnalysisResult> = listOf(
        dummyResult1,
        dummyResult2
    )

    // For ResultScreen Preview (Success State)
    val resultScreenSuccessUserImage = UserImage(
        localFilePath = "local/path_res_preview.jpg",
        fbFilePath = "https://firebasestorage.googleapis.com/v0/b/app.appspot.com/o/user_images%2Fres_preview.jpg?alt=media",
        createdAt = Date().time
    )

    val resultScreenSuccessTeenieping = TeeniepingInfo(
        id = "tp_res_preview_001",
        name = "해핑",
        description = "언제나 해맑은 해핑! 긍정 에너지로 주변을 밝게 만들어요. preview용 데이터",
        imagePath = "https://via.placeholder.com/180/FFFFAA/000000?Text=HaepingPreview"
    )

    val resultScreenSuccessShoppingLinks = listOf(
        ShoppingLink(itemName = "해핑 인형 (대형) - Preview", linkUrl = "https://example.com/doll_large_preview", storeName = "티니핑 스토어 Preview"),
        ShoppingLink(itemName = "해핑 키링 - Preview", linkUrl = "https://example.com/keyring_preview", storeName = "Another Store Preview")
    )

    val resultScreenSuccessResult = AnalysisResult(
        id = "res_preview_001",
        userImage = resultScreenSuccessUserImage,
        similarTeenieping = resultScreenSuccessTeenieping,
        similarityScore = 0.92f,
        analysisTimestamp = Date().time,
        shoppingLinks = resultScreenSuccessShoppingLinks
    )
} 