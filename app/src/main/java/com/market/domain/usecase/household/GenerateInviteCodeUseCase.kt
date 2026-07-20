package com.market.domain.usecase.household

import com.market.domain.repository.HouseholdRepository
import javax.inject.Inject

class GenerateInviteCodeUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String): Result<String> =
        householdRepository.generateInviteCode(householdId)
}
