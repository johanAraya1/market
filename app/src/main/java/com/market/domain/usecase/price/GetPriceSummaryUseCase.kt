package com.market.domain.usecase.price

import com.market.domain.model.Price
import com.market.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class PriceSummary(
    val itemId: String,
    val lowestPrice: Double,
    val highestPrice: Double,
    val averagePrice: Double,
    val storeCount: Int,
    val cheapestStoreId: String?
)

class GetPriceSummaryUseCase @Inject constructor(
    private val priceRepository: PriceRepository
) {
    operator fun invoke(householdId: String, itemId: String): Flow<PriceSummary?> {
        return priceRepository.observePrices(householdId, itemId).map { prices ->
            if (prices.isEmpty()) return@map null

            val amounts = prices.map { it.amount }
            val minPrice = amounts.min()
            val maxPrice = amounts.max()
            val avgPrice = amounts.average()
            val cheapest = prices.minByOrNull { it.amount }

            PriceSummary(
                itemId = itemId,
                lowestPrice = minPrice,
                highestPrice = maxPrice,
                averagePrice = avgPrice,
                storeCount = prices.size,
                cheapestStoreId = cheapest?.storeId
            )
        }
    }
}
