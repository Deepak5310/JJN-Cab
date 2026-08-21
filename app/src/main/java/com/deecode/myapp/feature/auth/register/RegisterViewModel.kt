package com.deecode.myapp.feature.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navigationChannel = Channel<RegisterNavigationEffect>(Channel.BUFFERED)
    val navigationEffect = _navigationChannel.receiveAsFlow()

    fun onEvent(event: RegisterUiEvent) {
        when (event) {
            is RegisterUiEvent.OnNameChange -> {
                _uiState.update { it.copy(name = event.name, nameError = null, generalError = null) }
            }
            is RegisterUiEvent.OnEmailChange -> {
                _uiState.update { it.copy(email = event.email, emailError = null, generalError = null) }
            }
            is RegisterUiEvent.OnPhoneChange -> {
                _uiState.update { it.copy(phone = event.phone, phoneError = null, generalError = null) }
            }
            is RegisterUiEvent.OnPasswordChange -> {
                _uiState.update { it.copy(password = event.password, passwordError = null, generalError = null) }
            }
            is RegisterUiEvent.OnConfirmPasswordChange -> {
                _uiState.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null, generalError = null) }
            }
            is RegisterUiEvent.OnTogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is RegisterUiEvent.OnToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            is RegisterUiEvent.ClearError -> {
                _uiState.update { it.copy(generalError = null) }
            }
            is RegisterUiEvent.OnRegisterClick -> {
                register()
            }
        }
    }

    private fun register() {
        if (_uiState.value.isLoading) return

        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val phone = _uiState.value.phone.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        var isValid = true

        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "Full name cannot be empty") }
            isValid = false
        }

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            isValid = false
        }

        if (phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Phone number cannot be empty") }
            isValid = false
        } else if (phone.length < 7) {
            _uiState.update { it.copy(phoneError = "Please enter a valid phone number") }
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            _uiState.update { it.copy(confirmPasswordError = "Please confirm your password") }
            isValid = false
        } else if (password != confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            when (val authResult = authRepository.signUpWithEmailPassword(email, password)) {
                is Resource.Success -> {
                    val authUser = authResult.data
                    val newUser = User(
                        uid = authUser.uid,
                        name = name,
                        email = email,
                        phone = phone,
                        role = UserRole.CUSTOMER
                    )

                    when (val profileResult = userRepository.createUserProfile(newUser)) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _navigationChannel.send(RegisterNavigationEffect.Success(UserRole.CUSTOMER))
                        }
                        is Resource.Error -> {
                            authRepository.signOut()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalError = "Account created but failed to initialize profile. Please try signing in."
                                )
                            }
                        }
                        is Resource.Loading -> Unit
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = authResult.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
