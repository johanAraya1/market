package com.market.domain.usecase.item

import com.market.domain.model.ShoppingItem
import com.market.domain.repository.ItemRepository
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(
        householdId: String,
        name: String,
        storeId: String? = null
    ): Result<ShoppingItem> {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("El nombre debe tener entre 1 y 50 caracteres"))
        }
        return itemRepository.addItem(householdId, trimmed, storeId)
    }
}
