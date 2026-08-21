package com.deecode.myapp.domain.model

data class DriverLocation(
    val bookingId: String,
    val driverId: String,
    val customerId: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Float = 0f,
    val speed: Float = 0f,
    val updatedAt: Long? = null
)
