package com.deecode.myapp.feature.customer.details

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.Vehicle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CustomerRideDetailsUiState(
    val isLoading: Boolean = true,
    val booking: Booking? = null,
    val driverName: String? = null,
    val driverVehicle: Vehicle? = null,
    val errorMessage: String? = null
) : UiState {
    val formattedFare: String
        get() {
            val fare = booking?.finalFare ?: booking?.estimatedFare ?: 0.0
            val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
            format.maximumFractionDigits = 0
            return format.format(fare)
        }

    val formattedDistance: String
        get() {
            val meters = booking?.finalDistanceMeters ?: booking?.distanceMeters ?: 0
            return if (meters < 1000) {
                "$meters m"
            } else {
                String.format(Locale.US, "%.1f km", meters / 1000.0)
            }
        }

    val formattedDuration: String
        get() {
            val seconds = booking?.finalDurationSeconds ?: booking?.estimatedDurationSeconds ?: 0L
            val minutes = seconds / 60
            return "$minutes min"
        }

    val formattedCreatedAt: String
        get() = formatDate(booking?.createdAt)

    val formattedCompletedAt: String
        get() = formatDate(booking?.completedAt)

    val formattedCancelledAt: String
        get() = formatDate(booking?.cancelledAt)

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0L) return ""
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

sealed interface CustomerRideDetailsUiEvent : UiEvent {
    data object Retry : CustomerRideDetailsUiEvent
    data object ClearError : CustomerRideDetailsUiEvent
}
