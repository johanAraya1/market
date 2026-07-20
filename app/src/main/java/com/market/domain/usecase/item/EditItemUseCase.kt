package com.market.domain.usecase.item

import com.market.domain.model.ShoppingItem
import com.market.domain.repository.ItemRepository
import javax.inject.Inject

class EditItemUseCase @Inject constructor(
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(item: ShoppingItem, newName: String): Result<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("El nombre debe tener entre 1 y 50 caracteres"))
        }
        return itemRepository.updateItem(item.copy(name = trimmed, updatedAt = System.currentTimeMillis()))
    }
}
