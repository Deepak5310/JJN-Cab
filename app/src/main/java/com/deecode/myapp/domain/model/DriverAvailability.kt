package com.deecode.myapp.domain.model

data class DriverAvailability(
    val driverId: String,
    val isOnline: Boolean = false,
    val updatedAt: Long? = null
)
