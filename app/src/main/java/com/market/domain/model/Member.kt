package com.market.domain.model

data class Member(
    val uid: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val displayName: String = "",
    val joinedAt: Long = 0L
)

enum class MemberRole {
    ADMIN,
    MEMBER
}
