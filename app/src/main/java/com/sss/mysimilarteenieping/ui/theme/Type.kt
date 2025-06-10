package com.sss.mysimilarteenieping.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.sss.mysimilarteenieping.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
val gamja_flower = GoogleFont("Gamja Flower")
val GamjaFlower = FontFamily(
    Font(
        googleFont = gamja_flower,
        fontProvider = provider,
    )
)


// 커스텀 폰트 패밀리 정의
// Gamja Flower 폰트는 기본적으로 굵기(Weight)를 지원하지 않으므로,
// 가독성을 위해 크기를 키우는 방식으로 조정합니다.
//val GamjaFlower = FontFamily(
//    Font(R.font.gamja_flower_regular, FontWeight.Normal)
//)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 59.sp, // +2sp
        lineHeight = 66.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 47.sp, // +2sp
        lineHeight = 54.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 38.sp, // +2sp
        lineHeight = 46.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp, // +2sp
        lineHeight = 42.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp, // +2sp
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp, // +2sp
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp, // +2sp
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    // 기본 폰트는 굵기를 올려 가독성을 확보합니다.
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Bold
        fontSize = 18.sp, // +2sp
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Bold
        fontSize = 16.sp, // +2sp
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // +2sp
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // +2sp
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Bold for better visibility
        fontSize = 14.sp, // +2sp
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GamjaFlower,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // +2sp
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Bold
        fontSize = 14.sp, // +2sp
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Bold
        fontSize = 13.sp, // +2sp
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
)