package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class LocationPointDto(
    @get:PropertyName("latitude") @set:PropertyName("latitude") var latitude: Double = 0.0,
    @get:PropertyName("longitude") @set:PropertyName("longitude") var longitude: Double = 0.0,
    @get:PropertyName("address") @set:PropertyName("address") var address: String? = null
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
    @get:PropertyName("bookingId") @set:PropertyName("bookingId") var bookingId: String = "",
    @get:PropertyName("customerId") @set:PropertyName("customerId") var customerId: String = "",
    @get:PropertyName("pickup") @set:PropertyName("pickup") var pickup: LocationPointDto = LocationPointDto(),
    @get:PropertyName("destination") @set:PropertyName("destination") var destination: LocationPointDto = LocationPointDto(),
    @get:PropertyName("distanceMeters") @set:PropertyName("distanceMeters") var distanceMeters: Int = 0,
    @get:PropertyName("estimatedDurationSeconds") @set:PropertyName("estimatedDurationSeconds") var estimatedDurationSeconds: Long = 0L,
    @get:PropertyName("estimatedFare") @set:PropertyName("estimatedFare") var estimatedFare: Double = 0.0,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = BookingStatus.REQUESTED.name,
    @get:PropertyName("driverId") @set:PropertyName("driverId") var driverId: String? = null,
    @ServerTimestamp
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null,
    @ServerTimestamp
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Timestamp? = null,
    @ServerTimestamp
    @get:PropertyName("completedAt") @set:PropertyName("completedAt") var completedAt: Timestamp? = null,
    @get:PropertyName("finalFare") @set:PropertyName("finalFare") var finalFare: Double? = null,
    @get:PropertyName("finalDistanceMeters") @set:PropertyName("finalDistanceMeters") var finalDistanceMeters: Int? = null,
    @get:PropertyName("finalDurationSeconds") @set:PropertyName("finalDurationSeconds") var finalDurationSeconds: Long? = null,
    @ServerTimestamp
    @get:PropertyName("cancelledAt") @set:PropertyName("cancelledAt") var cancelledAt: Timestamp? = null,
    @get:PropertyName("cancelledBy") @set:PropertyName("cancelledBy") var cancelledBy: String? = null,
    @get:PropertyName("cancellationReason") @set:PropertyName("cancellationReason") var cancellationReason: String? = null,
    @get:PropertyName("customerRating") @set:PropertyName("customerRating") var customerRating: Int? = null,
    @get:PropertyName("customerReview") @set:PropertyName("customerReview") var customerReview: String? = null,
    @get:PropertyName("driverRating") @set:PropertyName("driverRating") var driverRating: Int? = null,
    @get:PropertyName("driverReview") @set:PropertyName("driverReview") var driverReview: String? = null
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
        } catch (_: Exception) {
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
