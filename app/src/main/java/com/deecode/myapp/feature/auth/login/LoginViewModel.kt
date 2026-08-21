package com.deecode.myapp.feature.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationChannel = Channel<LoginNavigationEffect>(Channel.BUFFERED)
    val navigationEffect = _navigationChannel.receiveAsFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnEmailChange -> {
                _uiState.update { it.copy(email = event.email, emailError = null, generalError = null) }
            }
            is LoginUiEvent.OnPasswordChange -> {
                _uiState.update { it.copy(password = event.password, passwordError = null, generalError = null) }
            }
            is LoginUiEvent.OnTogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginUiEvent.ClearError -> {
                _uiState.update { it.copy(generalError = null) }
            }
            is LoginUiEvent.OnLoginClick -> {
                login()
            }
        }
    }

    private fun login() {
        if (_uiState.value.isLoading) return

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        var isValid = true
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            when (val authResult = authRepository.signInWithEmailPassword(email, password)) {
                is Resource.Success -> {
                    when (val profileResult = userRepository.getUserProfile(authResult.data.uid)) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _navigationChannel.send(LoginNavigationEffect.Success(profileResult.data.role))
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalError = profileResult.message
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
