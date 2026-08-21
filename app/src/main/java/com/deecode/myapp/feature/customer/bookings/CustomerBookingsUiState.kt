package com.deecode.myapp.feature.customer.bookings

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.Booking

data class CustomerBookingsUiState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val errorMessage: String? = null
) : UiState

sealed interface CustomerBookingsUiEvent : UiEvent {
    data object Refresh : CustomerBookingsUiEvent
    data object ClearError : CustomerBookingsUiEvent
}
