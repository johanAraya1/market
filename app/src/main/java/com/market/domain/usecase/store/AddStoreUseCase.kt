package com.market.domain.usecase.store

import com.market.domain.model.Store
import com.market.domain.repository.StoreRepository
import javax.inject.Inject

class AddStoreUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(householdId: String, name: String): Result<Store> {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("El nombre debe tener entre 1 y 50 caracteres"))
        }
        return storeRepository.addStore(householdId, trimmed)
    }
}
