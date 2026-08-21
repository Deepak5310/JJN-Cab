package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class LocationPointDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null
) {
    fun toDomain(): LocationPoint = LocationPoint(
        latitude = latitude,
        longitude = longitude,
        address = address
    )

    companion object {
        fun fromDomain(point: LocationPoint): LocationPointDto = LocationPointDto(
            latitude = point.latitude,
            longitude = point.longitude,
            address = point.address
        )
    }
}

data class BookingDto(
    @DocumentId
    val bookingId: String = "",
    val customerId: String = "",
    val pickup: LocationPointDto = LocationPointDto(),
    val destination: LocationPointDto = LocationPointDto(),
    val distanceMeters: Int = 0,
    val estimatedDurationSeconds: Long = 0L,
    val estimatedFare: Double = 0.0,
    val status: String = BookingStatus.REQUESTED.name,
    val driverId: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    @ServerTimestamp
    val completedAt: Timestamp? = null,
    val finalFare: Double? = null,
    val finalDistanceMeters: Int? = null,
    val finalDurationSeconds: Long? = null,
    @ServerTimestamp
    val cancelledAt: Timestamp? = null,
    val cancelledBy: String? = null,
    val cancellationReason: String? = null,
    val customerRating: Int? = null,
    val customerReview: String? = null,
    val driverRating: Int? = null,
    val driverReview: String? = null
) {
    fun toDomain(): Booking = Booking(
        bookingId = bookingId,
        customerId = customerId,
        pickup = pickup.toDomain(),
        destination = destination.toDomain(),
        distanceMeters = distanceMeters,
        estimatedDurationSeconds = estimatedDurationSeconds,
        estimatedFare = estimatedFare,
        status = try {
            when (status) {
                "ASSIGNED" -> BookingStatus.ACCEPTED
                "ARRIVING" -> BookingStatus.DRIVER_ARRIVING
                "STARTED" -> BookingStatus.IN_PROGRESS
                "CANCELLED" -> BookingStatus.CANCELLED
                else -> BookingStatus.valueOf(status)
            }
        } catch (e: Exception) {
            BookingStatus.REQUESTED
        },
        driverId = driverId,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
        completedAt = completedAt?.toDate()?.time,
        finalFare = finalFare,
        finalDistanceMeters = finalDistanceMeters,
        finalDurationSeconds = finalDurationSeconds,
        cancelledAt = cancelledAt?.toDate()?.time,
        cancelledBy = cancelledBy,
        cancellationReason = cancellationReason,
        customerRating = customerRating,
        customerReview = customerReview,
        driverRating = driverRating,
        driverReview = driverReview
    )

    companion object {
        fun fromDomain(booking: Booking): BookingDto = BookingDto(
            bookingId = booking.bookingId,
            customerId = booking.customerId,
            pickup = LocationPointDto.fromDomain(booking.pickup),
            destination = LocationPointDto.fromDomain(booking.destination),
            distanceMeters = booking.distanceMeters,
            estimatedDurationSeconds = booking.estimatedDurationSeconds,
            estimatedFare = booking.estimatedFare,
            status = booking.status.name,
            driverId = booking.driverId,
            finalFare = booking.finalFare,
            finalDistanceMeters = booking.finalDistanceMeters,
            finalDurationSeconds = booking.finalDurationSeconds,
            cancelledBy = booking.cancelledBy,
            cancellationReason = booking.cancellationReason,
            customerRating = booking.customerRating,
            customerReview = booking.customerReview,
            driverRating = booking.driverRating,
            driverReview = booking.driverReview
        )
    }
}
