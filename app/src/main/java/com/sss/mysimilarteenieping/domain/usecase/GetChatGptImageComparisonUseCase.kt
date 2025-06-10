package com.sss.mysimilarteenieping.domain.usecase

import android.graphics.Bitmap
import com.sss.mysimilarteenieping.data.model.TeeniepingInfo
import com.sss.mysimilarteenieping.data.repository.ChatGptRepository
import javax.inject.Inject

class GetChatGptImageComparisonUseCase @Inject constructor(
    private val chatGptRepository: ChatGptRepository
) {
    suspend operator fun invoke(
        userImage: Bitmap,
        teeniepingInfo: TeeniepingInfo
    ): Result<String> {
        return try {
            val prompt = """
                두 이미지를 비교하여 어떤 부분이 닮았는지 분석해주세요.
                
                첫 번째 이미지는 사용자가 업로드한 이미지이고,
                두 번째 이미지는 "${teeniepingInfo.name}"라는 티니핑 캐릭터입니다.
                
                다음과 같은 관점에서 분석해주세요:
                1. 얼굴형이나 윤곽
                2. 눈의 모양이나 표정
                3. 전체적인 분위기나 느낌
                4. 색감이나 톤
                
                아이들이 이해하기 쉽고 재미있게 설명해주세요.
                2-3문장으로 간단명료하게 작성해주세요.
                
                예시: "둥글둥글한 얼굴과 큰 눈이 ${teeniepingInfo.name}와 정말 닮았어요! 특히 밝고 맑은 표정이 똑같아서 깜짝 놀랐답니다."
            """.trimIndent()
            
            val result = chatGptRepository.getImageComparison(userImage, teeniepingInfo, prompt)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 