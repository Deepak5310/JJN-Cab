package com.deecode.myapp.feature.auth.register

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.core.model.UserRole

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null
) : UiState

sealed interface RegisterUiEvent : UiEvent {
    data class OnNameChange(val name: String) : RegisterUiEvent
    data class OnEmailChange(val email: String) : RegisterUiEvent
    data class OnPhoneChange(val phone: String) : RegisterUiEvent
    data class OnPasswordChange(val password: String) : RegisterUiEvent
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterUiEvent
    data object OnTogglePasswordVisibility : RegisterUiEvent
    data object OnToggleConfirmPasswordVisibility : RegisterUiEvent
    data object OnRegisterClick : RegisterUiEvent
    data object ClearError : RegisterUiEvent
}

sealed interface RegisterNavigationEffect {
    data class Success(val role: UserRole) : RegisterNavigationEffect
}
