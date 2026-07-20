package com.market.domain.usecase.store

import com.market.domain.repository.StoreRepository
import javax.inject.Inject

class DeleteStoreUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(householdId: String, storeId: String): Result<Unit> {
        return storeRepository.deleteStore(householdId, storeId)
    }
}
