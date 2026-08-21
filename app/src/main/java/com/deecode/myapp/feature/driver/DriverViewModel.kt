package com.deecode.myapp.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.DriverRepository
import com.deecode.myapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
    }

    private fun observeUserProfile() {
        val authUser = authRepository.currentUser
        if (authUser == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isUnauthorized = true,
                    errorMessage = "User session expired."
                )
            }
            return
        }

        viewModelScope.launch {
            userRepository.observeUserProfile(authUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val user = resource.data
                        val isAuthorized = user.role == UserRole.DRIVER || user.role == UserRole.ADMIN
                        _uiState.update {
                            it.copy(
                                user = user,
                                isLoading = false,
                                isUnauthorized = !isAuthorized,
                                errorMessage = if (!isAuthorized) "Access Denied: Driver account privileges required." else null
                            )
                        }
                        if (isAuthorized) {
                            observeDriverAvailability(user.uid)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun observeDriverAvailability(driverId: String) {
        viewModelScope.launch {
            driverRepository.observeAvailability(driverId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isOnline = resource.data.isOnline,
                                availabilityError = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(availabilityError = resource.message)
                        }
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun onEvent(event: DriverUiEvent) {
        when (event) {
            is DriverUiEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.tab) }
            is DriverUiEvent.ToggleOnlineStatus -> toggleOnlineStatus()
            is DriverUiEvent.Refresh -> observeUserProfile()
            is DriverUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is DriverUiEvent.ClearAvailabilityError -> _uiState.update { it.copy(availabilityError = null) }
        }
    }

    private fun toggleOnlineStatus() {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.isUpdatingAvailability || state.isUnauthorized) return

        val targetStatus = !state.isOnline

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingAvailability = true, availabilityError = null) }

            when (val result = driverRepository.setAvailability(user.uid, targetStatus)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingAvailability = false,
                            isOnline = targetStatus,
                            availabilityError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingAvailability = false,
                            availabilityError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }
}
