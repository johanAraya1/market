package com.market.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.market.domain.model.Household
import com.market.domain.model.Member
import com.market.domain.model.MemberRole
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseholdDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createHousehold(household: Household, member: Member): Household {
        val householdRef = firestore.collection("households").document()
        val memberRef = householdRef.collection("members").document(member.uid)

        firestore.runBatch { batch ->
            batch.set(householdRef, mapOf(
                "name" to household.name,
                "createdAt" to household.createdAt,
                "createdBy" to household.createdBy,
                "inviteCode" to null,
                "inviteCodeExpiry" to null
            ))
            batch.set(memberRef, mapOf(
                "role" to member.role.name,
                "displayName" to member.displayName,
                "joinedAt" to member.joinedAt
            ))
        }.await()

        return household.copy(id = householdRef.id)
    }

    suspend fun joinHousehold(inviteCode: String, member: Member): Household {
        // Find household by invite code
        val snapshot = firestore.collection("households")
            .whereEqualTo("inviteCode", inviteCode)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            throw IllegalArgumentException("Código inválido o expirado")
        }

        val householdDoc = snapshot.documents.first()
        val expiry = householdDoc.getLong("inviteCodeExpiry") ?: 0L
        if (expiry > 0 && System.currentTimeMillis() > expiry) {
            throw IllegalArgumentException("Código inválido o expirado")
        }

        val householdId = householdDoc.id
        val memberRef = firestore.collection("households")
            .document(householdId)
            .collection("members")
            .document(member.uid)

        memberRef.set(mapOf(
            "role" to member.role.name,
            "displayName" to member.displayName,
            "joinedAt" to member.joinedAt
        )).await()

        return Household(
            id = householdId,
            name = householdDoc.getString("name") ?: "",
            createdAt = householdDoc.getLong("createdAt") ?: 0L,
            createdBy = householdDoc.getString("createdBy") ?: ""
        )
    }

    suspend fun generateInviteCode(householdId: String): String {
        val code = (100000..999999).random().toString()
        val expiry = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // 7 days

        firestore.collection("households").document(householdId)
            .update(mapOf(
                "inviteCode" to code,
                "inviteCodeExpiry" to expiry
            )).await()

        return code
    }

    suspend fun getMembers(householdId: String): List<Member> {
        val snapshot = firestore.collection("households")
            .document(householdId)
            .collection("members")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Member(
                uid = doc.id,
                role = MemberRole.valueOf(doc.getString("role") ?: "MEMBER"),
                displayName = doc.getString("displayName") ?: "",
                joinedAt = doc.getLong("joinedAt") ?: 0L
            )
        }
    }

    suspend fun removeMember(householdId: String, uid: String) {
        firestore.collection("households")
            .document(householdId)
            .collection("members")
            .document(uid)
            .delete()
            .await()
    }

    suspend fun leaveHousehold(householdId: String, uid: String) {
        removeMember(householdId, uid)
    }

    suspend fun getHousehold(householdId: String): Household {
        val doc = firestore.collection("households")
            .document(householdId)
            .get()
            .await()

        return Household(
            id = doc.id,
            name = doc.getString("name") ?: "",
            createdAt = doc.getLong("createdAt") ?: 0L,
            createdBy = doc.getString("createdBy") ?: "",
            inviteCode = doc.getString("inviteCode"),
            inviteCodeExpiry = doc.getLong("inviteCodeExpiry")
        )
    }

    suspend fun getHouseholdByMember(uid: String): Household? {
        // Query all households where user is a member
        // This is a simplified approach — in production, use a collection group query
        val snapshot = firestore.collectionGroup("members")
            .whereEqualTo("__name__", uid)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return null

        val householdId = snapshot.documents.first().reference.parent.parent?.id ?: return null
        return getHousehold(householdId)
    }
}
