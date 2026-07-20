package com.market.domain.usecase.trip

import com.market.domain.model.Trip
import com.market.domain.repository.TripRepository
import javax.inject.Inject

class CompleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(trip: Trip): Result<Trip> {
        if (trip.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("La compra debe tener al menos un producto"))
        }
        if (trip.householdId.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe pertenecer a un hogar"))
        }
        return tripRepository.completeTrip(trip)
    }
}
