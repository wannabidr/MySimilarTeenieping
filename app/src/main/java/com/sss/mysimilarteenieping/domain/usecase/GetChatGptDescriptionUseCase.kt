package com.sss.mysimilarteenieping.domain.usecase

import com.sss.mysimilarteenieping.data.repository.ChatGptRepository
import javax.inject.Inject

class GetChatGptDescriptionUseCase @Inject constructor(
    private val chatGptRepository: ChatGptRepository
) {
    /**
     * 티니핑 이름과 선택적인 사용자 이미지 특징을 기반으로 ChatGPT로부터 설명을 가져옵니다.
     * @param teeniepingName 설명을 생성할 티니핑의 이름
     * @param userImageFeatures (선택 사항) 사용자 이미지에서 추출된 특징들, 설명 생성에 도움을 줄 수 있음
     * @return Result<String> 성공 시 설명을 담은 Result, 실패 시 오류를 담은 Result
     */
    suspend operator fun invoke(teeniepingName: String, userImageFeatures: String? = null): Result<String> {
        return chatGptRepository.generateDescription(teeniepingName, userImageFeatures)
    }
} 