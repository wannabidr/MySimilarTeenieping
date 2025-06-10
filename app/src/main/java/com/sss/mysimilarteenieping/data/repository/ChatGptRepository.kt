package com.sss.mysimilarteenieping.data.repository

import android.graphics.Bitmap
import com.sss.mysimilarteenieping.data.remote.ChatGptApiService
import com.sss.mysimilarteenieping.data.model.ChatGptRequest
import com.sss.mysimilarteenieping.data.model.ChatMessage
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ChatGptRepository {
    suspend fun generateDescription(teeniepingName: String, userImageFeatures: String? = null): Result<String>

    suspend fun getImageComparison(userImage: Bitmap, teeniepingInfo: TeeniepingInfo, prompt: String): Result<String>
}

class ChatGptRepositoryImpl @Inject constructor(
    private val chatGptApiService: ChatGptApiService
) : ChatGptRepository {

    override suspend fun generateDescription(teeniepingName: String, userImageFeatures: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = createPrompt(teeniepingName, userImageFeatures)
            
            val request = ChatGptRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "당신은 아이들을 위한 친근하고 재미있는 티니핑 설명가입니다. 아이들이 이해하기 쉽고 즐거워할 수 있도록 설명해주세요."
                    ),
                    ChatMessage(
                        role = "user",
                        content = prompt
                    )
                ),
                maxTokens = 250,
                temperature = 0.7
            )
            
            val response = chatGptApiService.createChatCompletion(request)
            
            if (response.isSuccessful) {
                val chatGptResponse = response.body()
                val description = chatGptResponse?.choices?.firstOrNull()?.message?.content
                
                if (description.isNullOrBlank()) {
                    Result.failure(Exception("ChatGPT 응답이 비어있습니다."))
                } else {
                    Result.success(description.trim())
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "API 키가 유효하지 않습니다."
                    429 -> "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
                    500, 502, 503 -> "ChatGPT 서버에 일시적인 문제가 있습니다."
                    else -> "ChatGPT API 호출 실패: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("ChatGPT API 호출 중 오류 발생: ${e.message}", e))
        }
    }
    
    private fun createPrompt(teeniepingName: String, userImageFeatures: String?): String {
        return buildString {
            append("다음 티니핑에 대해 아이들에게 친근하고 재미있게 설명해주세요: $teeniepingName")
            
            userImageFeatures?.let { features ->
                append("\n사용자 이미지 특징: $features")
                append("\n이 특징을 반영하여 왜 이 티니핑과 닮았는지 설명해주세요.")
            }
            
            append("\n\n설명은 다음 조건을 만족해야 합니다:")
            append("\n- 아이들이 이해하기 쉬운 언어 사용")
            append("\n- 티니핑의 특별한 능력이나 성격 포함")
            append("\n- 티니핑은 동물이 아니라 만화 캐릭터입니다. 만화 캐릭터 다운 스토리를 만들어 설명하세요.")
            append("\n- 긍정적이고 즐거운 톤")
            append("\n- 2-3문장으로 간단명료하게")
            append("\n- 반말로 친근하게 작성")
        }
    }

    override suspend fun getImageComparison(userImage: Bitmap, teeniepingInfo: TeeniepingInfo, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fallbackPrompt = """
                사용자가 "${teeniepingInfo.name}" 티니핑과 닮았다고 분석되었습니다.
                
                ${teeniepingInfo.name}의 특징:
                - 이름: ${teeniepingInfo.name}
                - 설명: ${teeniepingInfo.description}
                
                이 정보를 바탕으로 왜 사용자가 이 티니핑과 닮았는지 아이들에게 재미있게 설명해주세요.
                얼굴형, 표정, 분위기 등을 언급하며 2-3문장으로 간단하게 작성해주세요.
                
                예시: "둥글둥글한 얼굴과 큰 눈이 ${teeniepingInfo.name}와 정말 닮았어요! 특히 밝고 맑은 표정이 똑같아서 깜짝 놀랐답니다."
            """.trimIndent()
            
            val request = ChatGptRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "당신은 아이들을 위한 친근하고 재미있는 이미지 분석가입니다. 사용자와 캐릭터의 닮은 점을 아이들이 이해하기 쉽고 즐거워할 수 있도록 설명해주세요."
                    ),
                    ChatMessage(
                        role = "user",
                        content = fallbackPrompt
                    )
                ),
                maxTokens = 100,
                temperature = 0.8
            )
            
            val response = chatGptApiService.createChatCompletion(request)
            
            if (response.isSuccessful) {
                val chatGptResponse = response.body()
                val description = chatGptResponse?.choices?.firstOrNull()?.message?.content
                
                if (description.isNullOrBlank()) {
                    val fallbackDescription = "당신의 밝고 사랑스러운 표정이 ${teeniepingInfo.name}와 정말 닮았어요! 특히 눈에서 나오는 따뜻한 기운이 똑같답니다."
                    Result.success(fallbackDescription)
                } else {
                    Result.success(description.trim())
                }
            } else {
                val fallbackDescription = "당신의 사랑스러운 모습이 ${teeniepingInfo.name}와 정말 닮았어요! 밝은 표정과 따뜻한 분위기가 똑같답니다."
                Result.success(fallbackDescription)
            }
            
        } catch (e: Exception) {
            val fallbackDescription = "당신의 특별한 매력이 ${teeniepingInfo.name}와 정말 닮았어요! 긍정적인 에너지가 똑같이 느껴진답니다."
            Result.success(fallbackDescription)
        }
    }
} 