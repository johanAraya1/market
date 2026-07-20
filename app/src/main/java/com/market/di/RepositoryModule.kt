package com.market.di

import com.market.data.repository.AuthRepositoryImpl
import com.market.data.repository.HouseholdRepositoryImpl
import com.market.data.repository.ItemRepositoryImpl
import com.market.data.repository.PriceRepositoryImpl
import com.market.data.repository.StoreRepositoryImpl
import com.market.data.repository.TripRepositoryImpl
import com.market.domain.repository.AuthRepository
import com.market.domain.repository.HouseholdRepository
import com.market.domain.repository.ItemRepository
import com.market.domain.repository.PriceRepository
import com.market.domain.repository.StoreRepository
import com.market.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHouseholdRepository(impl: HouseholdRepositoryImpl): HouseholdRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(impl: PriceRepositoryImpl): PriceRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository
}
