package com.market.domain.usecase.trip

import com.market.domain.model.Trip
import com.market.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTripsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    operator fun invoke(householdId: String): Flow<List<Trip>> {
        return tripRepository.observeTrips(householdId)
    }
}
