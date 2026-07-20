package com.market.domain.model

data class TripItem(
    val name: String = "",
    val storeId: String? = null,
    val storeName: String? = null,
    val price: Double? = null,
    val quantity: Int = 1
)

data class Trip(
    val id: String = "",
    val householdId: String = "",
    val completedBy: String = "",
    val completedByName: String = "",
    val completedAt: Long = 0L,
    val totalEstimated: Double = 0.0,
    val items: List<TripItem> = emptyList()
)
