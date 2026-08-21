package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.DriverLocation
import com.deecode.myapp.domain.model.LocationPoint
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class DriverLocationDto(
    @DocumentId
    val bookingId: String = "",
    val driverId: String = "",
    val customerId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bearing: Float = 0f,
    val speed: Float = 0f,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun toDomain(): DriverLocation = DriverLocation(
        bookingId = bookingId,
        driverId = driverId,
        customerId = customerId,
        latitude = latitude,
        longitude = longitude,
        bearing = bearing,
        speed = speed,
        updatedAt = updatedAt?.toDate()?.time
    )

    fun toLocationPoint(): LocationPoint = LocationPoint(
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromDomain(
            bookingId: String,
            driverId: String,
            customerId: String,
            point: LocationPoint,
            bearing: Float = 0f,
            speed: Float = 0f
        ): DriverLocationDto = DriverLocationDto(
            bookingId = bookingId,
            driverId = driverId,
            customerId = customerId,
            latitude = point.latitude,
            longitude = point.longitude,
            bearing = bearing,
            speed = speed
        )
    }
}
