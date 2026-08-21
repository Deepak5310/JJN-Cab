package com.deecode.myapp.feature.admin

import com.deecode.myapp.core.base.UiEffect
import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState

data class AdminUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface AdminUiEvent : UiEvent {
    data object RefreshMetrics : AdminUiEvent
}

sealed interface AdminUiEffect : UiEffect {
    data class ShowToast(val message: String) : AdminUiEffect
}
