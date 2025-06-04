package com.sss.mysimilarteenieping.data.remote

/**
 * (선택 사항) ChatGPT API 연동을 위한 서비스 인터페이스
 */
interface ChatGptApiService {
    /**
     * 티니핑 이름과 사용자 사진 특징을 기반으로 설명을 생성 요청합니다.
     */
    suspend fun getTeeniepingDescription(
        teeniepingName: String,
        userImageFeatures: String // 예: "밝게 웃는 얼굴, 갈색 눈동자"
    ): Result<String>
}

/**
 * ChatGptApiService의 실제 구현체 (Retrofit 등 사용)
 */
class ChatGptApiServiceImpl(/* retrofit: Retrofit */) : ChatGptApiService {
    override suspend fun getTeeniepingDescription(teeniepingName: String, userImageFeatures: String): Result<String> {
        // TODO: Implement ChatGPT API call
        return Result.success("Generated description for $teeniepingName based on $userImageFeatures") // Placeholder
    }
} 