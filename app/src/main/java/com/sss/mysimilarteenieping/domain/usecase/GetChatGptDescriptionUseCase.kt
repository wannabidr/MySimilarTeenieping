package com.sss.mysimilarteenieping.domain.usecase

import com.sss.mysimilarteenieping.data.repository.ChatGptRepository
import javax.inject.Inject

class GetChatGptDescriptionUseCase @Inject constructor(
    private val chatGptRepository: ChatGptRepository
) {
    suspend operator fun invoke(teeniepingName: String, userImageFeatures: String? = null): Result<String> {
        return chatGptRepository.generateDescription(teeniepingName, userImageFeatures)
    }
} 