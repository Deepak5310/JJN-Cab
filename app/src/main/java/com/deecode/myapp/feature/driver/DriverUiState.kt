package com.deecode.myapp.feature.driver

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.User

enum class DriverTab {
    DASHBOARD,
    REQUESTS,
    ACTIVE_RIDE,
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
    val availabilityError: String? = null
) : UiState

sealed interface DriverUiEvent : UiEvent {
    data class SelectTab(val tab: DriverTab) : DriverUiEvent
    data object ToggleOnlineStatus : DriverUiEvent
    data object Refresh : DriverUiEvent
    data object ClearError : DriverUiEvent
    data object ClearAvailabilityError : DriverUiEvent
}
