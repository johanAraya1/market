package com.market.domain.usecase.price

import com.market.domain.model.Price
import com.market.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPriceHistoryUseCase @Inject constructor(
    private val priceRepository: PriceRepository
) {
    operator fun invoke(householdId: String, itemId: String): Flow<List<Price>> {
        return priceRepository.observePrices(householdId, itemId)
    }
}
