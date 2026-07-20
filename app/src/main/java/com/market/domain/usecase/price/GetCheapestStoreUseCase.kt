package com.market.domain.usecase.price

import com.market.domain.repository.PriceRepository
import javax.inject.Inject

class GetCheapestStoreUseCase @Inject constructor(
    private val priceRepository: PriceRepository
) {
    suspend operator fun invoke(
        householdId: String,
        itemId: String
    ): Result<Pair<String, Double>?> {
        if (itemId.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe seleccionar un producto"))
        }
        return priceRepository.getCheapestStore(householdId, itemId)
    }
}
