package com.deecode.myapp.feature.customer

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.User

enum class CustomerTab {
    HOME,
    BOOKINGS,
    PROFILE
}

data class CustomerUiState(
    val user: User? = null,
    val selectedTab: CustomerTab = CustomerTab.HOME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface CustomerUiEvent : UiEvent {
    data class SelectTab(val tab: CustomerTab) : CustomerUiEvent
    data object Refresh : CustomerUiEvent
    data object ClearError : CustomerUiEvent
}
