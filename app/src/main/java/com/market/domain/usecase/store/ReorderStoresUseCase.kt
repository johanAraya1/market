package com.market.domain.usecase.store

import com.market.domain.repository.StoreRepository
import javax.inject.Inject

class ReorderStoresUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(householdId: String, storeIds: List<String>): Result<Unit> {
        if (storeIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("La lista de tiendas no puede estar vacía"))
        }
        return storeRepository.reorderStores(householdId, storeIds)
    }
}
