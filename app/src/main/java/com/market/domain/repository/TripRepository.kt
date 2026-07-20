package com.market.domain.repository

import com.market.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun observeTrips(householdId: String): Flow<List<Trip>>
    fun observeTripDetail(householdId: String, tripId: String): Flow<Trip?>
    suspend fun completeTrip(trip: Trip): Result<Trip>
    suspend fun deleteTrip(householdId: String, tripId: String): Result<Unit>
}
