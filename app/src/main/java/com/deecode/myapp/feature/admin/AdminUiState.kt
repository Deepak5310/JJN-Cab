package com.deecode.myapp.feature.admin

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.User

enum class AdminTab {
    DASHBOARD,
    BOOKINGS,
    DRIVERS,
    USERS,
    PROFILE
}

data class AdminUiState(
    val user: User? = null,
    val selectedTab: AdminTab = AdminTab.DASHBOARD,
    val isLoading: Boolean = true,
    val isUnauthorized: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface AdminUiEvent : UiEvent {
    data class SelectTab(val tab: AdminTab) : AdminUiEvent
    data object Refresh : AdminUiEvent
    data object ClearError : AdminUiEvent
}
