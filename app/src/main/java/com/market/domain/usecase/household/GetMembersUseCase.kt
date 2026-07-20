package com.market.domain.usecase.household

import com.market.domain.model.Member
import com.market.domain.repository.HouseholdRepository
import javax.inject.Inject

class GetMembersUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository
) {
    suspend operator fun invoke(householdId: String): Result<List<Member>> =
        householdRepository.getMembers(householdId)
}
