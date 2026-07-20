package com.market.domain.usecase.item

import com.market.domain.repository.ItemRepository
import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(householdId: String, itemId: String): Result<Unit> {
        return itemRepository.deleteItem(householdId, itemId)
    }
}
