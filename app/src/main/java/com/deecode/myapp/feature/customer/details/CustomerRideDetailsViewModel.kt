package com.deecode.myapp.feature.customer.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import com.deecode.myapp.domain.repository.UserRepository
import com.deecode.myapp.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerRideDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val bookingId: String = try {
        savedStateHandle.toRoute<Route.CustomerRideDetailsRoute>().bookingId
    } catch (e: Exception) {
        savedStateHandle.get<String>("bookingId") ?: ""
    }

    private val _uiState = MutableStateFlow(CustomerRideDetailsUiState())
    val uiState: StateFlow<CustomerRideDetailsUiState> = _uiState.asStateFlow()

    private var detailsJob: Job? = null

    init {
        observeRideDetails()
    }

    private fun observeRideDetails() {
        if (bookingId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Booking reference not found."
                )
            }
            return
        }

        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "User session expired."
                )
            }
            return
        }

        detailsJob?.cancel()
        detailsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingRepository.observeBooking(bookingId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val booking = resource.data
                        if (booking.customerId != currentUser.uid) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Access Denied: You do not have permission to view this booking."
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    booking = booking,
                                    errorMessage = null
                                )
                            }
                            if (!booking.driverId.isNullOrBlank()) {
                                fetchDriverName(booking.driverId)
                            }
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

    private fun fetchDriverName(driverId: String) {
        viewModelScope.launch {
            when (val userRes = userRepository.getUserProfile(driverId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(driverName = userRes.data.name) }
                }
                else -> {
                    _uiState.update { it.copy(driverName = "Assigned Driver") }
                }
            }
        }
    }

    fun onEvent(event: CustomerRideDetailsUiEvent) {
        when (event) {
            is CustomerRideDetailsUiEvent.Retry -> observeRideDetails()
            is CustomerRideDetailsUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }
}
