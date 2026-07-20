package com.market.domain.model

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val householdId: String = "",
    val storeId: String? = null,
    val isChecked: Boolean = false,
    val checkedBy: String? = null,
    val checkedAt: Long? = null,
    val checkReason: String? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
