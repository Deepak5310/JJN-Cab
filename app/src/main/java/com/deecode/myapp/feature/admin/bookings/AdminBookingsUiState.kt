package com.deecode.myapp.feature.admin.bookings

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import java.util.Calendar

enum class BookingStatusFilter {
    ALL,
    ACTIVE,
    COMPLETED,
    CANCELLED
}

enum class BookingDateFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}

data class AdminBookingsUiState(
    val isLoading: Boolean = true,
    val isUnauthorized: Boolean = false,
    val allBookings: List<Booking> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: BookingStatusFilter = BookingStatusFilter.ALL,
    val dateFilter: BookingDateFilter = BookingDateFilter.ALL,
    val selectedBooking: Booking? = null,
    val isCancelling: Boolean = false,
    val actionMessage: String? = null,
    val errorMessage: String? = null
) : UiState {
    val filteredBookings: List<Booking>
        get() {
            val now = System.currentTimeMillis()
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val startOfWeek = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            return allBookings.filter { booking ->
                val matchesStatus = when (statusFilter) {
                    BookingStatusFilter.ALL -> true
                    BookingStatusFilter.ACTIVE -> booking.status.isActive
                    BookingStatusFilter.COMPLETED -> booking.status == BookingStatus.COMPLETED
                    BookingStatusFilter.CANCELLED -> booking.status in setOf(
                        BookingStatus.CANCELLED,
                        BookingStatus.CANCELLED_BY_CUSTOMER,
                        BookingStatus.CANCELLED_BY_DRIVER
                    )
                }

                val matchesDate = when (dateFilter) {
                    BookingDateFilter.ALL -> true
                    BookingDateFilter.TODAY -> booking.createdAt >= startOfToday
                    BookingDateFilter.THIS_WEEK -> booking.createdAt >= startOfWeek
                    BookingDateFilter.THIS_MONTH -> booking.createdAt >= startOfMonth
                }

                val query = searchQuery.trim().lowercase()
                val matchesQuery = query.isBlank() ||
                        booking.bookingId.lowercase().contains(query) ||
                        booking.customerId.lowercase().contains(query) ||
                        (booking.driverId?.lowercase()?.contains(query) == true) ||
                        (booking.pickup.address?.lowercase()?.contains(query) == true) ||
                        (booking.destination.address?.lowercase()?.contains(query) == true)

                matchesStatus && matchesDate && matchesQuery
            }
        }
}

sealed interface AdminBookingsUiEvent : UiEvent {
    data class SearchQueryChanged(val query: String) : AdminBookingsUiEvent
    data class SelectStatusFilter(val filter: BookingStatusFilter) : AdminBookingsUiEvent
    data class SelectDateFilter(val filter: BookingDateFilter) : AdminBookingsUiEvent
    data class SelectBooking(val booking: Booking?) : AdminBookingsUiEvent
    data class CancelBookingAsAdmin(val bookingId: String, val reason: String) : AdminBookingsUiEvent
    data object Refresh : AdminBookingsUiEvent
    data object ClearActionMessage : AdminBookingsUiEvent
    data object ClearError : AdminBookingsUiEvent
}
