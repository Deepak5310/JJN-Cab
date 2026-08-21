package com.deecode.myapp.feature.customer.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import com.deecode.myapp.domain.repository.DriverTrackingRepository
import com.deecode.myapp.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerActiveBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val driverTrackingRepository: DriverTrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerActiveBookingUiState())
    val uiState: StateFlow<CustomerActiveBookingUiState> = _uiState.asStateFlow()

    private var driverTrackingJob: Job? = null

    init {
        _uiState.update { it.copy(hasLocationPermission = locationRepository.hasLocationPermission()) }
        observeActiveBooking()
    }

    private fun observeActiveBooking() {
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "User session expired. Please log in again."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingRepository.observeActiveCustomerBooking(currentUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val booking = resource.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                booking = booking,
                                errorMessage = null
                            )
                        }
                        syncDriverTracking(booking)
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

    private fun syncDriverTracking(booking: Booking?) {
        if (booking != null && booking.driverId != null) {
            if (driverTrackingJob == null || driverTrackingJob?.isActive == false) {
                driverTrackingJob = viewModelScope.launch {
                    driverTrackingRepository.observeDriverLocation(booking.bookingId).collect { locRes ->
                        if (locRes is Resource.Success) {
                            val loc = locRes.data
                            _uiState.update {
                                it.copy(
                                    driverLocation = loc?.let { dl ->
                                        LocationPoint(
                                            latitude = dl.latitude,
                                            longitude = dl.longitude
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            driverTrackingJob?.cancel()
            driverTrackingJob = null
            _uiState.update { it.copy(driverLocation = null) }
        }
    }

    fun onEvent(event: CustomerActiveBookingUiEvent) {
        when (event) {
            is CustomerActiveBookingUiEvent.Retry -> observeActiveBooking()
            is CustomerActiveBookingUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is CustomerActiveBookingUiEvent.CancelBooking -> cancelBooking(event.bookingId, event.reason)
            is CustomerActiveBookingUiEvent.ClearCancellationError -> _uiState.update { it.copy(cancellationError = null) }
        }
    }

    private fun cancelBooking(bookingId: String, reason: String) {
        if (_uiState.value.isCancelling) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, cancellationError = null) }

            when (val result = bookingRepository.cancelBooking(bookingId, reason)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            cancellationError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            cancellationError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        driverTrackingJob?.cancel()
    }
}
