package com.deecode.myapp.feature.driver

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.User

enum class DriverTab {
    DASHBOARD,
    REQUESTS,
    ACTIVE_RIDE,
    HISTORY,
    PROFILE
}

data class DriverUiState(
    val user: User? = null,
    val selectedTab: DriverTab = DriverTab.DASHBOARD,
    val isLoading: Boolean = true,
    val isUnauthorized: Boolean = false,
    val errorMessage: String? = null,
    val isOnline: Boolean = false,
    val isUpdatingAvailability: Boolean = false,
    val availabilityError: String? = null,
    val pendingBookings: List<Booking> = emptyList(),
    val isLoadingRequests: Boolean = false,
    val requestsError: String? = null,
    val acceptingBookingId: String? = null,
    val dismissedBookingIds: Set<String> = emptySet(),
    val actionMessage: String? = null,
    val activeDriverBooking: Booking? = null,
    val activeCustomerName: String? = null,
    val isUpdatingRideStatus: Boolean = false,
    val rideStatusError: String? = null,
    val isRatingSheetVisible: Boolean = false,
    val isSubmittingRating: Boolean = false,
    val ratingError: String? = null,
    val isRatingSubmitted: Boolean = false
) : UiState

sealed interface DriverUiEvent : UiEvent {
    data class SelectTab(val tab: DriverTab) : DriverUiEvent
    data object ToggleOnlineStatus : DriverUiEvent
    data object Refresh : DriverUiEvent
    data object ClearError : DriverUiEvent
    data object ClearAvailabilityError : DriverUiEvent
    data object RefreshRequests : DriverUiEvent
    data class AcceptBooking(val bookingId: String) : DriverUiEvent
    data class RejectBooking(val bookingId: String) : DriverUiEvent
    data object ClearActionMessage : DriverUiEvent
    data class UpdateRideStatus(val bookingId: String, val newStatus: BookingStatus) : DriverUiEvent
    data class CompleteBooking(val bookingId: String) : DriverUiEvent
    data class CancelBooking(val bookingId: String, val reason: String) : DriverUiEvent
    data object ClearRideStatusError : DriverUiEvent
    data object OpenRatingSheet : DriverUiEvent
    data object CloseRatingSheet : DriverUiEvent
    data class SubmitCustomerRating(val rating: Int, val review: String?) : DriverUiEvent
}
