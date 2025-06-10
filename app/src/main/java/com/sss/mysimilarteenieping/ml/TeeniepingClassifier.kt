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

class TeeniepingClassifier(
    private val context: Context,
    private val modelPath: String = "keras_model_term_05.tflite",
    private val labelPath: String = "teenieping_labels.txt"
) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0
    private var modelInputChannel: Int = 0
    private var modelOutputClasses: Int = 0

    companion object {
        private const val TAG = "TeeniepingClassifier"
    }

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

    private fun initModelShape() {
        interpreter?.let { interp ->
            val inputTensor: Tensor = interp.getInputTensor(0)
            val inputShape: IntArray = inputTensor.shape()
            
            Log.d(TAG, "Input tensor shape: ${inputShape.contentToString()}")
            
            when {
                inputShape.size >= 4 -> {
                    modelInputHeight = inputShape[1]
                    modelInputWidth = inputShape[2]
                    modelInputChannel = inputShape[3]
                }
                inputShape.size >= 3 -> {
                    modelInputHeight = inputShape[0]
                    modelInputWidth = inputShape[1]
                    modelInputChannel = inputShape[2]
                }
                else -> {
                    Log.w(TAG, "Unexpected input shape size: ${inputShape.size}, using default values")
                    modelInputHeight = 320
                    modelInputWidth = 320
                    modelInputChannel = 3
                }
            }

            val outputTensor: Tensor = interp.getOutputTensor(0)
            val outputShape: IntArray = outputTensor.shape()
            
            Log.d(TAG, "Output tensor shape: ${outputShape.contentToString()}")
            
            modelOutputClasses = when {
                outputShape.size >= 2 && outputShape[1] > 0 -> outputShape[1]
                outputShape.size >= 1 && outputShape[0] > 0 -> outputShape[0]
                else -> {
                    Log.w(TAG, "Unexpected output shape, using default classes: 100")
                    100
                }
            }
        }
    }

    private fun loadLabels() {
        try {
            labels = context.assets.open(labelPath).bufferedReader().useLines { it.toList() }
        } catch (e: IOException) {
            Log.w(TAG, "Could not load labels from $labelPath, using default labels", e)
            labels = listOf(
                "하츄핑", "라라핑", "바로핑", "차차핑", "키키핑", 
                "무무핑", "아하핑", "옴뇸핑", "따라핑", "토들핑"
            )
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        if (modelInputWidth <= 0 || modelInputHeight <= 0) {
            Log.w(TAG, "Invalid model input size: ${modelInputWidth}x${modelInputHeight}, using default 320x320")
            return Bitmap.createScaledBitmap(bitmap, 320, 320, false)
        }
        
        Log.d(TAG, "Resizing bitmap from ${bitmap.width}x${bitmap.height} to ${modelInputWidth}x${modelInputHeight}")
        return Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, false)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val normalizedR = r / 255.0f
            val normalizedG = g / 255.0f
            val normalizedB = b / 255.0f

            byteBuffer.putFloat(normalizedR)
            byteBuffer.putFloat(normalizedG)
            byteBuffer.putFloat(normalizedB)
        }

        return byteBuffer
    }

    fun classify(bitmap: Bitmap): Pair<TeeniepingInfo?, Float> {
        return try {
            val buffer = convertBitmapToByteBuffer(resizeBitmap(bitmap))
            val result = Array(1) { FloatArray(modelOutputClasses) }

            interpreter?.run(buffer, result)

            val (classIndex, confidence) = argmax(result[0])
            
            if (classIndex < labels.size) {
                val teeniepingName = labels[classIndex]
                val teeniepingInfo = TeeniepingInfo(
                    id = classIndex,
                    name = teeniepingName,
                    description = "${teeniepingName}과 ${String.format("%.1f", confidence * 100)}% 닮았어요!",
                    imagePath = "teenieping_images/${teeniepingName}.jpg"
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

    private fun argmax(array: FloatArray): Pair<Int, Float> {
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

    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "TeeniepingClassifier closed")
    }
} 