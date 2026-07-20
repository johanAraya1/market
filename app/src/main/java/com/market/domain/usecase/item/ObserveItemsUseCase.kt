package com.market.domain.usecase.item

import com.market.domain.model.ShoppingItem
import com.market.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveItemsUseCase @Inject constructor(
    private val itemRepository: ItemRepository
) {
    operator fun invoke(householdId: String): Flow<List<ShoppingItem>> {
        return itemRepository.observeItems(householdId)
    }
}
