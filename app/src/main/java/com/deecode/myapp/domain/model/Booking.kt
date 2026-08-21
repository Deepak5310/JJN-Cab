package com.deecode.myapp.domain.model

data class Booking(
    val bookingId: String = "",
    val customerId: String,
    val pickup: LocationPoint,
    val destination: LocationPoint,
    val distanceMeters: Int,
    val estimatedDurationSeconds: Long,
    val estimatedFare: Double,
    val status: BookingStatus = BookingStatus.REQUESTED,
    val driverId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val finalFare: Double? = null,
    val finalDistanceMeters: Int? = null,
    val finalDurationSeconds: Long? = null
)
