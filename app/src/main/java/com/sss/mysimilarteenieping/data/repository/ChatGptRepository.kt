package com.sss.mysimilarteenieping.data.repository

import com.sss.mysimilarteenieping.data.remote.ChatGptApiService
import com.sss.mysimilarteenieping.data.model.ChatGptRequest
import com.sss.mysimilarteenieping.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ChatGptRepository {
    /**
     * 주어진 티니핑 이름과 선택적 사용자 이미지 특징을 기반으로 설명을 생성합니다.
     *
     * @param teeniepingName 설명을 생성할 티니핑의 이름
     * @param userImageFeatures (선택 사항) 사용자 이미지에서 추출된 특징들
     * @return Result<String> 성공 시 설명을 담은 Result, 실패 시 오류를 담은 Result
     */
    suspend fun generateDescription(teeniepingName: String, userImageFeatures: String? = null): Result<String>
}

class ChatGptRepositoryImpl @Inject constructor(
    private val chatGptApiService: ChatGptApiService
) : ChatGptRepository {

    override suspend fun generateDescription(teeniepingName: String, userImageFeatures: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. 프롬프트 생성
            val prompt = createPrompt(teeniepingName, userImageFeatures)
            
            // 2. ChatGPT API 요청 생성
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
                maxTokens = 150,
                temperature = 0.7
            )
            
            // 3. API 호출
            val response = chatGptApiService.createChatCompletion(request)
            
            // 4. 응답 처리
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
            append("티니핑의 이름이 숫자라면 임의의 이름을 만들어 티니핑을 설명해주세요.")            
            append("\n\n설명은 다음 조건을 만족해야 합니다:")
            append("\n- 아이들이 이해하기 쉬운 언어 사용")
            append("\n- 티니핑의 특별한 능력이나 성격 포함")
            append("\n- 긍정적이고 즐거운 톤")
            append("\n- 2-3문장으로 간단명료하게")
        }
    }
} 