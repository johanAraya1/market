package com.market.domain.usecase.price

import com.market.domain.model.Price
import com.market.domain.repository.PriceRepository
import javax.inject.Inject

class RecordPriceUseCase @Inject constructor(
    private val priceRepository: PriceRepository
) {
    suspend operator fun invoke(
        householdId: String,
        itemId: String,
        storeId: String,
        amount: Double
    ): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("El precio debe ser mayor a cero"))
        }
        if (storeId.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe seleccionar una tienda"))
        }
        if (itemId.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe seleccionar un producto"))
        }

        val price = Price(
            itemId = itemId,
            storeId = storeId,
            amount = amount,
            currency = "CRC"
        )
        return priceRepository.recordPrice(householdId, price)
    }
}
