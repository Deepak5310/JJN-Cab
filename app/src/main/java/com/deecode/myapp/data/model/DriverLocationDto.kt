package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.DriverLocation
import com.deecode.myapp.domain.model.LocationPoint
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class DriverLocationDto(
    @DocumentId
    @get:PropertyName("bookingId") @set:PropertyName("bookingId") var bookingId: String = "",
    @get:PropertyName("driverId") @set:PropertyName("driverId") var driverId: String = "",
    @get:PropertyName("customerId") @set:PropertyName("customerId") var customerId: String = "",
    @get:PropertyName("latitude") @set:PropertyName("latitude") var latitude: Double = 0.0,
    @get:PropertyName("longitude") @set:PropertyName("longitude") var longitude: Double = 0.0,
    @get:PropertyName("bearing") @set:PropertyName("bearing") var bearing: Float = 0f,
    @get:PropertyName("speed") @set:PropertyName("speed") var speed: Float = 0f,
    @ServerTimestamp
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Timestamp? = null
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
