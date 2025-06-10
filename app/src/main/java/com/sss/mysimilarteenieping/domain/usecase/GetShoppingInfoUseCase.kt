package com.sss.mysimilarteenieping.domain.usecase

import com.sss.mysimilarteenieping.data.model.ShoppingLink
import com.sss.mysimilarteenieping.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShoppingInfoUseCase @Inject constructor(
    private val shoppingRepository: ShoppingRepository
) {
    operator fun invoke(query: String): Flow<List<ShoppingLink>> {
        return shoppingRepository.getShoppingInfo(query)
    }
}