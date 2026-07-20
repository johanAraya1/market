package com.market.domain.usecase.household

import com.market.domain.model.Household
import com.market.domain.repository.HouseholdRepository
import javax.inject.Inject

class JoinHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository
) {
    suspend operator fun invoke(inviteCode: String): Result<Household> {
        val trimmed = inviteCode.trim()
        if (trimmed.length != 6) {
            return Result.failure(IllegalArgumentException("Código inválido o expirado"))
        }
        return householdRepository.joinHousehold(trimmed)
    }
}
