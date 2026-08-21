package com.deecode.myapp.feature.driver

import com.deecode.myapp.core.base.UiEffect
import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState

data class DriverUiState(
    val isOnline: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface DriverUiEvent : UiEvent {
    data class ToggleOnlineStatus(val isOnline: Boolean) : DriverUiEvent
}

sealed interface DriverUiEffect : UiEffect {
    data class ShowToast(val message: String) : DriverUiEffect
}
