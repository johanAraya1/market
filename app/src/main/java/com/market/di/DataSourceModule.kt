package com.market.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    // All data sources use @Inject constructor + @Singleton.
    // No @Provides needed — Hilt resolves them via constructor injection.
}
