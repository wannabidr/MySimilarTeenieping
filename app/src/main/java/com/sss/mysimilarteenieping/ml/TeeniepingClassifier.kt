package com.sss.mysimilarteenieping.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * TensorFlow Lite 모델을 사용하여 이미지에서 유사한 티니핑을 분류합니다.
 */
class TeeniepingClassifier(
    private val context: Context,
    private val modelPath: String = "keras_model_term_05.tflite", // assets 폴더 내 모델 파일 경로
    private val labelPath: String = "teenieping_labels.txt"  // assets 폴더 내 레이블 파일 경로
) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    
    // 모델 입력/출력 크기 정보
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0
    private var modelInputChannel: Int = 0
    private var modelOutputClasses: Int = 0

    companion object {
        private const val TAG = "TeeniepingClassifier"
    }

    /**
     * 모델을 초기화합니다.
     */
    @Throws(IOException::class)
    fun init() {
        val model = loadModelFile(modelPath)
        model.order(ByteOrder.nativeOrder())
        interpreter = Interpreter(model)
        
        initModelShape()
        loadLabels()
        
        Log.d(TAG, "TeeniepingClassifier initialized successfully")
        Log.d(TAG, "Model input size: ${modelInputWidth}x${modelInputHeight}x${modelInputChannel}")
        Log.d(TAG, "Model output classes: $modelOutputClasses")
        Log.d(TAG, "Labels loaded: ${labels.size}")
    }

    /**
     * assets 폴더에서 TFLite 모델 파일을 로드합니다.
     */
    @Throws(IOException::class)
    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val assetManager: AssetManager = context.assets
        val assetFileDescriptor: AssetFileDescriptor = assetManager.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * 모델의 입력/출력 형태를 초기화합니다.
     */
    private fun initModelShape() {
        interpreter?.let { interp ->
            val inputTensor: Tensor = interp.getInputTensor(0)
            val inputShape: IntArray = inputTensor.shape()
            
            // 디버그: 실제 입력 형태 로그 출력
            Log.d(TAG, "Input tensor shape: ${inputShape.contentToString()}")
            
            // 배열 크기 확인 후 안전하게 접근
            when {
                inputShape.size >= 4 -> {
                    // [batch_size, height, width, channels] (NHWC)
                    modelInputHeight = inputShape[1]
                    modelInputWidth = inputShape[2]
                    modelInputChannel = inputShape[3]
                }
                inputShape.size >= 3 -> {
                    // [height, width, channels] (HWC) - batch size가 제거된 경우
                    modelInputHeight = inputShape[0]
                    modelInputWidth = inputShape[1]
                    modelInputChannel = inputShape[2]
                }
                else -> {
                    // 예상치 못한 형태의 경우 기본값 설정
                    Log.w(TAG, "Unexpected input shape size: ${inputShape.size}, using default values")
                    modelInputHeight = 320  // 기본값
                    modelInputWidth = 320   // 기본값
                    modelInputChannel = 3   // 기본값 (RGB)
                }
            }

            val outputTensor: Tensor = interp.getOutputTensor(0)
            val outputShape: IntArray = outputTensor.shape()
            
            // 디버그: 실제 출력 형태 로그 출력
            Log.d(TAG, "Output tensor shape: ${outputShape.contentToString()}")
            
            // 출력 형태 안전하게 처리
            modelOutputClasses = when {
                outputShape.size >= 2 && outputShape[1] > 0 -> outputShape[1]  // [batch_size, num_classes]
                outputShape.size >= 1 && outputShape[0] > 0 -> outputShape[0]  // [num_classes]
                else -> {
                    Log.w(TAG, "Unexpected output shape, using default classes: 100")
                    100  // 기본값 (라벨 파일의 클래스 수와 맞춤)
                }
            }
        }
    }

    /**
     * 라벨 파일을 로드합니다.
     */
    private fun loadLabels() {
        try {
            labels = context.assets.open(labelPath).bufferedReader().useLines { it.toList() }
        } catch (e: IOException) {
            Log.w(TAG, "Could not load labels from $labelPath, using default labels", e)
            // 기본 티니핑 라벨들 (실제 모델에 맞게 수정 필요)
            labels = listOf(
                "하츄핑", "라라핑", "바로핑", "차차핑", "키키핑", 
                "무무핑", "아하핑", "옴뇸핑", "따라핑", "토들핑"
            )
        }
    }

    /**
     * 비트맵을 모델 입력 크기에 맞게 리사이즈합니다.
     */
    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        // 입력 크기 유효성 검사
        if (modelInputWidth <= 0 || modelInputHeight <= 0) {
            Log.w(TAG, "Invalid model input size: ${modelInputWidth}x${modelInputHeight}, using default 320x320")
            return Bitmap.createScaledBitmap(bitmap, 320, 320, false)
        }
        
        Log.d(TAG, "Resizing bitmap from ${bitmap.width}x${bitmap.height} to ${modelInputWidth}x${modelInputHeight}")
        return Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, false)
    }

    /**
     * 비트맵을 RGB ByteBuffer로 변환합니다.
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // 각 픽셀당 3개의 float (R, G, B)를 저장하므로, 전체 바이트 크기는 픽셀 수 * 3 * float의 바이트 크기 (4)
        val byteBuffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF // Red 채널
            val g = (pixel shr 8) and 0xFF  // Green 채널
            val b = pixel and 0xFF          // Blue 채널

            // 각 채널 값을 0.0f ~ 1.0f 사이로 정규화
            val normalizedR = r / 255.0f
            val normalizedG = g / 255.0f
            val normalizedB = b / 255.0f

            byteBuffer.putFloat(normalizedR)
            byteBuffer.putFloat(normalizedG)
            byteBuffer.putFloat(normalizedB)
        }

        return byteBuffer
    }

    /**
     * 주어진 비트맵 이미지를 분석하여 가장 유사한 티니핑 정보와 유사도 점수를 반환합니다.
     *
     * @param bitmap 분석할 사용자 이미지
     * @return Pair<TeeniepingInfo?, Float> (가장 유사한 티니핑 정보, 유사도 점수). 찾지 못하면 (null, 0.0f).
     */
    fun classify(bitmap: Bitmap): Pair<TeeniepingInfo?, Float> {
        return try {
            val buffer = convertBitmapToByteBuffer(resizeBitmap(bitmap))
            val result = Array(1) { FloatArray(modelOutputClasses) }

            interpreter?.run(buffer, result)

            val (classIndex, confidence) = argmax(result[0])
            
            if (classIndex < labels.size) {
                val teeniepingName = labels[classIndex]
                val teeniepingInfo = TeeniepingInfo(
                    id = teeniepingName,
                    name = teeniepingName,
                    description = "${teeniepingName}과 ${String.format("%.1f", confidence * 100)}% 닮았어요!",
                    imagePath = "teenieping_images/${teeniepingName.lowercase()}.png" // assets 내 이미지 경로
                )
                Pair(teeniepingInfo, confidence)
            } else {
                Log.w(TAG, "Class index $classIndex is out of bounds for labels size ${labels.size}")
                Pair(null, 0.0f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification", e)
            Pair(null, 0.0f)
        }
    }

    /**
     * 배열에서 최대값과 그 인덱스를 찾습니다.
     */
    private fun argmax(array: FloatArray): Pair<Int, Float> {
        // 빈 배열 처리
        if (array.isEmpty()) {
            Log.w(TAG, "Empty array passed to argmax")
            return Pair(0, 0.0f)
        }
        
        var argmax = 0
        var max = array[0]
        
        for (i in 1 until array.size) {
            val value = array[i]
            if (value > max) {
                argmax = i
                max = value
            }
        }
        
        return Pair(argmax, max)
    }

    /**
     * 분류기를 정리하고 리소스를 해제합니다.
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "TeeniepingClassifier closed")
    }
} 