package com.sss.mysimilarteenieping.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

/**
 * 이미지 URI로부터 EXIF 방향 정보를 읽어 올바르게 회전된 비트맵을 생성합니다.
 *
 * @param context 컨텍스트
 * @param imageUri 이미지의 URI
 * @return 방향이 보정된 비트맵. 오류 발생 시 null을 반환할 수 있습니다.
 */
fun getCorrectlyOrientedBitmap(context: Context, imageUri: Uri): Bitmap? {
    try {
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            context.contentResolver.openInputStream(imageUri)?.use { exifInputStream ->
                val exifInterface = ExifInterface(exifInputStream)
                val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    else -> return bitmap 
                }
                
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
} 