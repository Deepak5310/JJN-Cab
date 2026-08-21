package com.deecode.myapp.feature.admin.dashboard

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.AdminDashboardStats
import com.deecode.myapp.domain.model.Booking

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val isUnauthorized: Boolean = false,
    val stats: AdminDashboardStats = AdminDashboardStats(),
    val recentBookings: List<Booking> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface AdminDashboardUiEvent : UiEvent {
    data object Refresh : AdminDashboardUiEvent
    data object ClearError : AdminDashboardUiEvent
}
