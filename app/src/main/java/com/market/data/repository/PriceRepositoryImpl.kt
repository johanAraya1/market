package com.market.data.repository

import com.market.data.remote.AuthDataSource
import com.market.data.remote.PriceDataSource
import com.market.domain.model.Price
import com.market.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepositoryImpl @Inject constructor(
    private val priceDataSource: PriceDataSource,
    private val authDataSource: AuthDataSource
) : PriceRepository {

    override fun observePrices(householdId: String, itemId: String): Flow<List<Price>> {
        return priceDataSource.observePrices(householdId, itemId)
    }

    override fun observeAllPrices(householdId: String): Flow<List<Price>> {
        return priceDataSource.observeAllPrices(householdId)
    }

    override suspend fun recordPrice(householdId: String, price: Price): Result<Unit> = runCatching {
        val currentUser = authDataSource.getCurrentFirebaseUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        val priceToSave = price.copy(recordedBy = currentUser.uid)
        priceDataSource.recordPrice(priceToSave, householdId)
    }

    override suspend fun getCheapestStore(
        householdId: String,
        itemId: String
    ): Result<Pair<String, Double>?> = runCatching {
        priceDataSource.getCheapestStore(householdId, itemId)
    }
}
