package com.market.domain.model

data class Household(
    val id: String = "",
    val name: String = "",
    val createdAt: Long = 0L,
    val createdBy: String = "",
    val inviteCode: String? = null,
    val inviteCodeExpiry: Long? = null
)
