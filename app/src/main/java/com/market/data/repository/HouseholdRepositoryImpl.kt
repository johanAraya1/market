package com.market.data.repository

import com.market.data.remote.HouseholdDataSource
import com.market.domain.model.Household
import com.market.domain.model.Member
import com.market.domain.model.MemberRole
import com.market.domain.repository.HouseholdRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseholdRepositoryImpl @Inject constructor(
    private val householdDataSource: HouseholdDataSource,
    private val authDataSource: com.market.data.remote.AuthDataSource
) : HouseholdRepository {

    override suspend fun createHousehold(name: String): Result<Household> = runCatching {
        val currentUser = authDataSource.getCurrentFirebaseUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        val now = System.currentTimeMillis()
        val household = Household(
            name = name,
            createdAt = now,
            createdBy = currentUser.uid
        )
        val adminMember = Member(
            uid = currentUser.uid,
            role = MemberRole.ADMIN,
            displayName = currentUser.displayName ?: "",
            joinedAt = now
        )
        householdDataSource.createHousehold(household, adminMember)
    }

    override suspend fun joinHousehold(inviteCode: String): Result<Household> = runCatching {
        val currentUser = authDataSource.getCurrentFirebaseUser()
            ?: throw IllegalStateException("No hay usuario autenticado")

        val member = Member(
            uid = currentUser.uid,
            role = MemberRole.MEMBER,
            displayName = currentUser.displayName ?: "",
            joinedAt = System.currentTimeMillis()
        )
        householdDataSource.joinHousehold(inviteCode, member)
    }

    override suspend fun generateInviteCode(householdId: String): Result<String> = runCatching {
        householdDataSource.generateInviteCode(householdId)
    }

    override suspend fun getMembers(householdId: String): Result<List<Member>> = runCatching {
        householdDataSource.getMembers(householdId)
    }

    override suspend fun removeMember(householdId: String, uid: String): Result<Unit> = runCatching {
        householdDataSource.removeMember(householdId, uid)
    }

    override suspend fun leaveHousehold(householdId: String): Result<Unit> = runCatching {
        val currentUser = authDataSource.getCurrentFirebaseUser()
            ?: throw IllegalStateException("No hay usuario autenticado")
        householdDataSource.leaveHousehold(householdId, currentUser.uid)
    }

    override suspend fun getHousehold(householdId: String): Result<Household> = runCatching {
        householdDataSource.getHousehold(householdId)
    }
}
