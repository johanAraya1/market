package com.market.domain.usecase.store

import com.market.domain.model.Store
import com.market.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    operator fun invoke(householdId: String): Flow<List<Store>> {
        return storeRepository.observeStores(householdId)
    }
}
