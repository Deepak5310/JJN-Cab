package com.deecode.myapp.feature.customer.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import com.deecode.myapp.domain.repository.DriverTrackingRepository
import com.deecode.myapp.domain.repository.LocationRepository
import com.deecode.myapp.domain.repository.RatingRepository
import com.deecode.myapp.domain.repository.VehicleRepository
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
    private val driverTrackingRepository: DriverTrackingRepository,
    private val ratingRepository: RatingRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerActiveBookingUiState())
    val uiState: StateFlow<CustomerActiveBookingUiState> = _uiState.asStateFlow()

    private var driverTrackingJob: Job? = null
    private var driverVehicleJob: Job? = null

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
                                isRatingSubmitted = booking?.customerRating != null,
                                errorMessage = null
                            )
                        }
                        syncDriverTracking(booking)
                        syncDriverVehicle(booking?.driverId)
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

    private fun syncDriverVehicle(driverId: String?) {
        if (driverId.isNullOrBlank()) {
            driverVehicleJob?.cancel()
            driverVehicleJob = null
            _uiState.update { it.copy(driverVehicle = null) }
            return
        }

        if (driverVehicleJob == null || driverVehicleJob?.isActive != true) {
            driverVehicleJob = viewModelScope.launch {
                vehicleRepository.observeDriverVehicle(driverId).collect { resource ->
                    if (resource is Resource.Success) {
                        _uiState.update { it.copy(driverVehicle = resource.data) }
                    }
                }
            }
        }
    }

    private fun syncDriverTracking(booking: Booking?) {
        val shouldTrack = booking != null &&
                booking.driverId != null &&
                (booking.status == BookingStatus.DRIVER_ARRIVING || booking.status == BookingStatus.IN_PROGRESS)

        if (shouldTrack) {
            if (driverTrackingJob == null || driverTrackingJob?.isActive != true) {
                driverTrackingJob = viewModelScope.launch {
                    driverTrackingRepository.observeDriverLocation(booking.bookingId)
                        .collect { resource ->
                            if (resource is Resource.Success) {
                                val loc = resource.data?.let {
                                    LocationPoint(latitude = it.latitude, longitude = it.longitude)
                                }
                                _uiState.update { it.copy(driverLocation = loc) }
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
            is CustomerActiveBookingUiEvent.CancelBooking -> cancelRide(event.bookingId, event.reason)
            is CustomerActiveBookingUiEvent.ClearCancellationError -> _uiState.update { it.copy(cancellationError = null) }
            is CustomerActiveBookingUiEvent.OpenRatingSheet -> _uiState.update { it.copy(isRatingSheetVisible = true, ratingError = null) }
            is CustomerActiveBookingUiEvent.CloseRatingSheet -> _uiState.update { it.copy(isRatingSheetVisible = false, ratingError = null) }
            is CustomerActiveBookingUiEvent.SubmitRating -> submitRating(event.rating, event.review)
        }
    }

    private fun submitRating(rating: Int, review: String?) {
        val booking = _uiState.value.booking ?: return
        val currentUid = authRepository.currentUser?.uid ?: return
        val driverId = booking.driverId ?: return

        if (_uiState.value.isSubmittingRating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingRating = true, ratingError = null) }

            val result = ratingRepository.submitRating(
                bookingId = booking.bookingId,
                fromUserId = currentUid,
                toUserId = driverId,
                role = "CUSTOMER",
                rating = rating,
                review = review
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingRating = false,
                            isRatingSheetVisible = false,
                            isRatingSubmitted = true,
                            ratingError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingRating = false,
                            ratingError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun cancelRide(bookingId: String, reason: String) {
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
        driverVehicleJob?.cancel()
    }
}
