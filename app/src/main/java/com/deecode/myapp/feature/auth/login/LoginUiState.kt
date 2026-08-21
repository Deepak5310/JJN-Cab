package com.deecode.myapp.feature.auth.login

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.core.model.UserRole

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null
) : UiState

sealed interface LoginUiEvent : UiEvent {
    data class OnEmailChange(val email: String) : LoginUiEvent
    data class OnPasswordChange(val password: String) : LoginUiEvent
    data object OnTogglePasswordVisibility : LoginUiEvent
    data object OnLoginClick : LoginUiEvent
    data object ClearError : LoginUiEvent
}

sealed interface LoginNavigationEffect {
    data class Success(val role: UserRole) : LoginNavigationEffect
}
