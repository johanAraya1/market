package com.market.domain.repository

import com.market.domain.model.Household
import com.market.domain.model.Member

interface HouseholdRepository {
    suspend fun createHousehold(name: String): Result<Household>
    suspend fun joinHousehold(inviteCode: String): Result<Household>
    suspend fun generateInviteCode(householdId: String): Result<String>
    suspend fun getMembers(householdId: String): Result<List<Member>>
    suspend fun removeMember(householdId: String, uid: String): Result<Unit>
    suspend fun leaveHousehold(householdId: String): Result<Unit>
    suspend fun getHousehold(householdId: String): Result<Household>
}
