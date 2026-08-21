package com.deecode.myapp.domain.model

data class AdminDashboardStats(
    val totalCustomers: Int = 0,
    val totalDrivers: Int = 0,
    val onlineDrivers: Int = 0,
    val activeBookings: Int = 0,
    val completedRides: Int = 0,
    val cancelledRides: Int = 0,
    val totalRevenue: Double = 0.0
)
