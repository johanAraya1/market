package com.market.domain.repository

import com.market.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeStores(householdId: String): Flow<List<Store>>
    suspend fun addStore(householdId: String, name: String): Result<Store>
    suspend fun updateStore(store: Store): Result<Unit>
    suspend fun deleteStore(householdId: String, storeId: String): Result<Unit>
    suspend fun reorderStores(householdId: String, storeIds: List<String>): Result<Unit>
}
