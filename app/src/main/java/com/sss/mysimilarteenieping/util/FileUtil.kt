package com.sss.mysimilarteenieping.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 이미지 저장을 위한 고유한 이름의 임시 파일을 생성합니다.
 * 파일은 앱의 외부 캐시 디렉토리에 저장됩니다.
 *
 * @param context 컨텍스트
 * @return 생성된 이미지 파일 객체
 */
fun createImageFile(context: Context): File {
    // 파일 이름에 타임스탬프를 사용하여 고유성 보장
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.externalCacheDir
    
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg",               /* suffix */
        storageDir            /* directory */
    )
} 