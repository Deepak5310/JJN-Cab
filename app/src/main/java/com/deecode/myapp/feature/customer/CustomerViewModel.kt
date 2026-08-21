package com.deecode.myapp.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.LocationRepository
import com.deecode.myapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState(isLoading = true))
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
    }

    private fun observeUserProfile() {
        val authUser = authRepository.currentUser
        if (authUser == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User session expired.") }
            return
        }

        viewModelScope.launch {
            userRepository.observeUserProfile(authUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                user = resource.data,
                                isLoading = false,
                                errorMessage = null
                            )
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

    fun onEvent(event: CustomerUiEvent) {
        when (event) {
            is CustomerUiEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is CustomerUiEvent.Refresh -> {
                observeUserProfile()
            }
            is CustomerUiEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is CustomerUiEvent.RequestLocation -> {
                fetchCurrentLocation()
            }
            is CustomerUiEvent.OnLocationPermissionDenied -> {
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        isPermissionPermanentlyDenied = event.permanentlyDenied,
                        locationError = if (event.permanentlyDenied) {
                            "Location permission permanently denied. Please enable it in App Settings."
                        } else {
                            "Location permission was denied. Location is needed to determine your pickup point."
                        }
                    )
                }
            }
            is CustomerUiEvent.ClearLocationError -> {
                _uiState.update { it.copy(locationError = null) }
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        return locationRepository.hasLocationPermission()
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, locationError = null) }

            when (val result = locationRepository.getCurrentLocation()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            currentLocation = result.data,
                            isLocating = false,
                            locationError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLocating = false,
                            locationError = result.message
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
