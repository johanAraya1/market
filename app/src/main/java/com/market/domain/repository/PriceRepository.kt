package com.market.domain.repository

import com.market.domain.model.Price
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    fun observePrices(householdId: String, itemId: String): Flow<List<Price>>
    fun observeAllPrices(householdId: String): Flow<List<Price>>
    suspend fun recordPrice(householdId: String, price: Price): Result<Unit>
    suspend fun getCheapestStore(householdId: String, itemId: String): Result<Pair<String, Double>?>
}
