package com.market.domain.model

data class Store(
    val id: String = "",
    val name: String = "",
    val householdId: String = "",
    val order: Int = 0,
    val createdAt: Long = 0L
)
