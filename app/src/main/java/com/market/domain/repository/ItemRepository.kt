package com.market.domain.repository

import com.market.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observeItems(householdId: String): Flow<List<ShoppingItem>>
    suspend fun addItem(householdId: String, name: String, storeId: String?): Result<ShoppingItem>
    suspend fun updateItem(item: ShoppingItem): Result<Unit>
    suspend fun deleteItem(householdId: String, itemId: String): Result<Unit>
}
