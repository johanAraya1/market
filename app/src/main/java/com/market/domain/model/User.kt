package com.market.domain.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
    val lastLoginAt: Long = 0L
)
