package com.market.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.market.data.remote.AuthDataSource
import com.market.data.remote.HouseholdDataSource
import com.market.data.remote.ItemDataSource
import com.market.data.remote.PriceDataSource
import com.market.data.remote.StoreDataSource
import com.market.data.remote.TripDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideAuthDataSource(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthDataSource = AuthDataSource(firebaseAuth, firestore)

    @Provides
    @Singleton
    fun provideHouseholdDataSource(
        firestore: FirebaseFirestore
    ): HouseholdDataSource = HouseholdDataSource(firestore)

    @Provides
    @Singleton
    fun provideItemDataSource(
        firestore: FirebaseFirestore
    ): ItemDataSource = ItemDataSource(firestore)

    @Provides
    @Singleton
    fun provideStoreDataSource(
        firestore: FirebaseFirestore
    ): StoreDataSource = StoreDataSource(firestore)

    @Provides
    @Singleton
    fun providePriceDataSource(
        firestore: FirebaseFirestore
    ): PriceDataSource = PriceDataSource(firestore)

    @Provides
    @Singleton
    fun provideTripDataSource(
        firestore: FirebaseFirestore
    ): TripDataSource = TripDataSource(firestore)
}
