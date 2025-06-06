package com.sss.mysimilarteenieping.domain.usecase

import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 특정 검색어(예: 티니핑 이름)에 대한 쇼핑 정보를 가져오는 UseCase
 */
class GetShoppingInfoUseCase @Inject constructor(
    private val shoppingRepository: ShoppingRepository
) {
    operator fun invoke(query: String): Flow<List<ShoppingLink>> {
        return shoppingRepository.getShoppingInfo(query)
    }
}