package com.market.data.repository

import com.market.data.remote.AuthDataSource
import com.market.data.remote.TripDataSource
import com.market.domain.model.Trip
import com.market.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDataSource: TripDataSource,
    private val authDataSource: AuthDataSource
) : TripRepository {

    override fun observeTrips(householdId: String): Flow<List<Trip>> {
        return tripDataSource.observeTrips(householdId)
    }

    override fun observeTripDetail(householdId: String, tripId: String): Flow<Trip?> {
        return tripDataSource.observeTripDetail(householdId, tripId)
    }

    override suspend fun completeTrip(trip: Trip): Result<Trip> = runCatching {
        val currentUser = authDataSource.getCurrentFirebaseUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        val tripToSave = trip.copy(
            completedBy = currentUser.uid,
            completedByName = currentUser.displayName ?: ""
        )
        tripDataSource.completeTrip(tripToSave)
    }

    override suspend fun deleteTrip(householdId: String, tripId: String): Result<Unit> = runCatching {
        tripDataSource.deleteTrip(householdId, tripId)
    }
}
