package com.deecode.myapp.feature.driver.history

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.Booking
import java.text.NumberFormat
import java.util.Locale

data class DriverHistoryUiState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val todayEarnings: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val completedCount: Int = 0,
    val cancelledCount: Int = 0,
    val errorMessage: String? = null
) : UiState {

    val formattedTodayEarnings: String
        get() = formatCurrency(todayEarnings)

    val formattedTotalEarnings: String
        get() = formatCurrency(totalEarnings)

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}

sealed interface DriverHistoryUiEvent : UiEvent {
    data object Refresh : DriverHistoryUiEvent
    data object ClearError : DriverHistoryUiEvent
}
