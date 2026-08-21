package com.deecode.myapp.feature.customer.active

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.LocationPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CustomerActiveBookingUiState(
    val isLoading: Boolean = true,
    val booking: Booking? = null,
    val driverLocation: LocationPoint? = null,
    val hasLocationPermission: Boolean = false,
    val isCancelling: Boolean = false,
    val cancellationError: String? = null,
    val isRatingSheetVisible: Boolean = false,
    val isSubmittingRating: Boolean = false,
    val ratingError: String? = null,
    val isRatingSubmitted: Boolean = false,
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
        get() {
            val time = booking?.createdAt ?: 0L
            if (time == 0L) return ""
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            return sdf.format(Date(time))
        }
}

sealed interface CustomerActiveBookingUiEvent : UiEvent {
    data object Retry : CustomerActiveBookingUiEvent
    data object ClearError : CustomerActiveBookingUiEvent
    data class CancelBooking(val bookingId: String, val reason: String) : CustomerActiveBookingUiEvent
    data object ClearCancellationError : CustomerActiveBookingUiEvent
    data object OpenRatingSheet : CustomerActiveBookingUiEvent
    data object CloseRatingSheet : CustomerActiveBookingUiEvent
    data class SubmitRating(val rating: Int, val review: String?) : CustomerActiveBookingUiEvent
}
