package com.market.domain.usecase.trip

import com.market.domain.repository.TripRepository
import javax.inject.Inject

class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(householdId: String, tripId: String): Result<Unit> {
        if (tripId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de viaje inválido"))
        }
        return tripRepository.deleteTrip(householdId, tripId)
    }
}
