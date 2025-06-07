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
        id = 0,
        name = "방글핑",
        description = "언제나 방긋 웃는 행복의 티니핑!",
        imagePath = "https://via.placeholder.com/150/FFAACC/000000?Text=Banggleping"
    )

    val dummyResult1 = AnalysisResult(
        id = "result001",
        userImage = dummyUserImage1,
        similarTeenieping = dummyTeenieping1.copy(
            description = dummyTeenieping1.description, // 원래 설명 유지
            details = "방글핑은 매우 긍정적이고 웃음이 많은 티니핑이랍니다. 주변에 행복을 나눠주는 것을 좋아해요! (ChatGPT 설명)"
        ),
        similarityScore = 0.85f,
        analysisTimestamp = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
        shoppingLinks = listOf(
            ShoppingLink(
                itemName = "방글핑 플러시 인형",
                linkUrl = "https://shopping.naver.com/window-products/8394728001",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394728/83947280001.jpg",
                storeName = "티니핑 스토어"
            ),
            ShoppingLink(
                itemName = "방글핑 키링",
                linkUrl = "https://shopping.naver.com/window-products/8394728002",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394728/83947280002.jpg",
                storeName = "캐릭터 월드"
            )
        ),
        chatGptDescription = "둥글둥글한 얼굴과 밝은 미소가 방글핑과 정말 닮았어요! 특히 반짝이는 눈과 따뜻한 표정이 똑같아서 깜짝 놀랐답니다."
    )

    val dummyUserImage2 = UserImage(
        localFilePath = "/path/to/user_image2.jpg",
        fbFilePath = "https://firebasestorage.googleapis.com/v0/b/app.appspot.com/o/user_images%2Fimage2.jpg?alt=media",
        createdAt = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000 // 1 day ago
    )

    val dummyTeenieping2 = TeeniepingInfo(
        id = 1,
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
        shoppingLinks = listOf(
            ShoppingLink(
                itemName = "믿어핑 피규어",
                linkUrl = "https://shopping.naver.com/window-products/8394729001",
                itemImageUrl = "https://shopping.phinf.naver.net/main_8394729/83947290001.jpg",
                storeName = "믿을만한 쇼핑몰"
            )
        ),
        chatGptDescription = "진지하고 신뢰감 있는 표정이 믿어핑과 완전히 똑같아요! 당신의 든든한 모습이 믿어핑의 특징을 그대로 보여주고 있답니다."
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
        id = 2,
        name = "해핑",
        description = "언제나 해맑은 해핑! 긍정 에너지로 주변을 밝게 만들어요. preview용 데이터",
        imagePath = "https://via.placeholder.com/180/FFFFAA/000000?Text=HaepingPreview"
    )

    val previewShoppingLinks = listOf(
        ShoppingLink(itemName = "해핑 인형 (대형) - Preview", itemImageUrl = "https://shopping.phinf.naver.net/main_preview/preview_doll.jpg", linkUrl = "https://shopping.naver.com/window-products/preview-001", storeName = "티니핑 스토어 Preview"),
        ShoppingLink(itemName = "해핑 키링 - Preview", itemImageUrl = "https://shopping.phinf.naver.net/main_preview/preview_keyring.jpg", linkUrl = "https://shopping.naver.com/window-products/preview-002", storeName = "Another Store Preview")
    )

    val resultScreenSuccessResult = AnalysisResult(
        id = "res_preview_001",
        userImage = resultScreenSuccessUserImage,
        similarTeenieping = resultScreenSuccessTeenieping.copy(
            description = resultScreenSuccessTeenieping.description, // 원래 설명 유지
            details = "해핑은 반짝이는 햇살처럼 밝고 따뜻한 마음을 가졌어요. 언제나 친구들에게 웃음과 행복을 선물한답니다! (ChatGPT 설명)"
        ),
        similarityScore = 0.92f,
        analysisTimestamp = Date().time,
        shoppingLinks = previewShoppingLinks,
        chatGptDescription = "햇살처럼 밝은 미소와 반짝이는 눈이 해핑과 정말 똑같아요! 당신의 긍정적인 에너지가 해핑의 따뜻함과 완전히 일치한답니다."
    )
}