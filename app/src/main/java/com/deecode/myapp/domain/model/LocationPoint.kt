package com.deecode.myapp.domain.model

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
