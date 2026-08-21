package com.deecode.myapp.domain.model

data class Vehicle(
    val driverId: String,
    val vehicleType: String = "SEDAN", // SEDAN, HATCHBACK, SUV, AUTO
    val makeModel: String = "",
    val registrationNumber: String = "",
    val color: String = "",
    val updatedAt: Long? = null
) {
    val formattedPlate: String
        get() = registrationNumber.trim().uppercase()
}
