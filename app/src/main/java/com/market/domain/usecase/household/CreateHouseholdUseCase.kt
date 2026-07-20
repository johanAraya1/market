package com.market.domain.usecase.household

import com.market.domain.model.Household
import com.market.domain.repository.HouseholdRepository
import javax.inject.Inject

class CreateHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository
) {
    suspend operator fun invoke(name: String): Result<Household> {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("El nombre debe tener entre 1 y 50 caracteres"))
        }
        return householdRepository.createHousehold(trimmed)
    }
}
