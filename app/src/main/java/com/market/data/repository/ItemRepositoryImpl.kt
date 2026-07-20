package com.market.data.repository

import com.market.data.remote.AuthDataSource
import com.market.data.remote.ItemDataSource
import com.market.domain.model.ShoppingItem
import com.market.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDataSource: ItemDataSource,
    private val authDataSource: AuthDataSource
) : ItemRepository {

    override fun observeItems(householdId: String): Flow<List<ShoppingItem>> {
        return itemDataSource.observeItems(householdId)
    }

    override suspend fun addItem(
        householdId: String,
        name: String,
        storeId: String?
    ): Result<ShoppingItem> = runCatching {
        val currentUser = authDataSource.getCurrentFirebaseUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        val item = ShoppingItem(
            name = name,
            householdId = householdId,
            storeId = storeId,
            createdBy = currentUser.uid
        )
        itemDataSource.addItem(item)
    }

    override suspend fun updateItem(item: ShoppingItem): Result<Unit> = runCatching {
        itemDataSource.updateItem(item)
    }

    override suspend fun deleteItem(householdId: String, itemId: String): Result<Unit> = runCatching {
        itemDataSource.deleteItem(householdId, itemId)
    }
}
