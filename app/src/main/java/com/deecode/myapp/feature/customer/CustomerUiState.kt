package com.deecode.myapp.feature.customer

import com.deecode.myapp.core.base.UiEffect
import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState

data class CustomerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface CustomerUiEvent : UiEvent {
    data object Refresh : CustomerUiEvent
}

sealed interface CustomerUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : CustomerUiEffect
}
