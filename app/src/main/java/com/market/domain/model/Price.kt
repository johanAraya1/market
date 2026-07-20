package com.market.domain.model

data class Price(
    val itemId: String = "",
    val storeId: String = "",
    val amount: Double = 0.0,
    val currency: String = "CRC",
    val recordedBy: String = "",
    val recordedAt: Long = 0L
)
