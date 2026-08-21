package com.deecode.myapp.domain.model

data class DriverManagementItem(
    val driverId: String,
    val user: User,
    val isOnline: Boolean = false,
    val vehicleModel: String = "Maruti Suzuki Dzire",
    val vehiclePlate: String = "DL 01 AB 1234",
    val completedTrips: Int = 0,
    val cancelledTrips: Int = 0,
    val todayEarnings: Double = 0.0,
    val totalEarnings: Double = 0.0
)
