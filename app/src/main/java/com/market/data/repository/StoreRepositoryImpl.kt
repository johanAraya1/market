package com.market.data.repository

import com.market.data.remote.StoreDataSource
import com.market.domain.model.Store
import com.market.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val storeDataSource: StoreDataSource
) : StoreRepository {

    override fun observeStores(householdId: String): Flow<List<Store>> {
        return storeDataSource.observeStores(householdId)
    }

    override suspend fun addStore(householdId: String, name: String): Result<Store> = runCatching {
        val store = Store(name = name, householdId = householdId)
        storeDataSource.addStore(store)
    }

    override suspend fun updateStore(store: Store): Result<Unit> = runCatching {
        storeDataSource.updateStore(store)
    }

    override suspend fun deleteStore(householdId: String, storeId: String): Result<Unit> = runCatching {
        storeDataSource.deleteStore(householdId, storeId)
    }

    override suspend fun reorderStores(householdId: String, storeIds: List<String>): Result<Unit> = runCatching {
        storeDataSource.reorderStores(householdId, storeIds)
    }
}
