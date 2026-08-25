package com.deecode.myapp.feature.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import com.deecode.myapp.domain.repository.DriverRepository
import com.deecode.myapp.domain.repository.DriverTrackingRepository
import com.deecode.myapp.domain.repository.LocationRepository
import com.deecode.myapp.domain.repository.NotificationRepository
import com.deecode.myapp.domain.repository.RatingRepository
import com.deecode.myapp.domain.repository.UserRepository
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
class DriverViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val driverRepository: DriverRepository,
    private val bookingRepository: BookingRepository,
    private val locationRepository: LocationRepository,
    private val driverTrackingRepository: DriverTrackingRepository,
    private val notificationRepository: NotificationRepository,
    private val ratingRepository: RatingRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private var pendingRequestsJob: Job? = null
    private var customerProfileJob: Job? = null
    private var locationTrackingJob: Job? = null
    private var vehicleJob: Job? = null
    private var availabilityJob: Job? = null
    private var activeBookingJob: Job? = null

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
                            observeActiveDriverBooking(user.uid)
                            observeDriverVehicle(user.uid)
                            notificationRepository.syncFcmToken(user.uid)
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

    private fun observeDriverVehicle(driverId: String) {
        vehicleJob?.cancel()
        vehicleJob = viewModelScope.launch {
            vehicleRepository.observeDriverVehicle(driverId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(vehicle = resource.data) }
                }
            }
        }
    }

    private fun observeDriverAvailability(driverId: String) {
        availabilityJob?.cancel()
        availabilityJob = viewModelScope.launch {
            driverRepository.observeAvailability(driverId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val isOnline = resource.data.isOnline
                        _uiState.update {
                            it.copy(
                                isOnline = isOnline,
                                availabilityError = null
                            )
                        }
                        syncRequestsStream(isOnline)
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

    private fun observeActiveDriverBooking(driverId: String) {
        activeBookingJob?.cancel()
        activeBookingJob = viewModelScope.launch {
            bookingRepository.observeActiveDriverBooking(driverId).collect { resource ->
                if (resource is Resource.Success) {
                    val booking = resource.data
                    _uiState.update { it.copy(activeDriverBooking = booking) }
                    if (booking != null) {
                        fetchCustomerName(booking.customerId)
                        syncLocationTracking(booking, driverId)
                    } else {
                        customerProfileJob?.cancel()
                        stopLocationTracking()
                        _uiState.update { it.copy(activeCustomerName = null) }
                    }
                }
            }
        }
    }

    private fun syncLocationTracking(booking: Booking, driverId: String) {
        val isTrackingActive = booking.status == BookingStatus.DRIVER_ARRIVING ||
                booking.status == BookingStatus.IN_PROGRESS

        if (isTrackingActive) {
            if (locationTrackingJob == null || locationTrackingJob?.isActive == false) {
                locationTrackingJob = viewModelScope.launch {
                    try {
                        locationRepository.observeLocationUpdates().collect { point ->
                            driverTrackingRepository.pushDriverLocation(
                                bookingId = booking.bookingId,
                                driverId = driverId,
                                customerId = booking.customerId,
                                location = point
                            )
                        }
                    } catch (e: Exception) {
                        // Location permission denied or GPS disabled handled gracefully
                    }
                }
            }
        } else {
            stopLocationTracking()
        }
    }

    private fun stopLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
    }

    private fun fetchCustomerName(customerId: String) {
        customerProfileJob?.cancel()
        customerProfileJob = viewModelScope.launch {
            when (val userRes = userRepository.getUserProfile(customerId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(activeCustomerName = userRes.data.name) }
                }
                else -> {
                    _uiState.update { it.copy(activeCustomerName = "Passenger") }
                }
            }
        }
    }

    private fun syncRequestsStream(isOnline: Boolean) {
        if (isOnline) {
            if (pendingRequestsJob == null || pendingRequestsJob?.isActive == false) {
                pendingRequestsJob = viewModelScope.launch {
                    _uiState.update { it.copy(isLoadingRequests = true, requestsError = null) }
                    bookingRepository.observePendingBookings().collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                _uiState.update {
                                    it.copy(
                                        pendingBookings = resource.data,
                                        isLoadingRequests = false,
                                        requestsError = null
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoadingRequests = false,
                                        requestsError = resource.message
                                    )
                                }
                            }
                            is Resource.Loading -> {
                                _uiState.update { it.copy(isLoadingRequests = true) }
                            }
                        }
                    }
                }
            }
        } else {
            pendingRequestsJob?.cancel()
            pendingRequestsJob = null
            _uiState.update {
                it.copy(
                    pendingBookings = emptyList(),
                    isLoadingRequests = false,
                    requestsError = null
                )
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
            is DriverUiEvent.RefreshRequests -> syncRequestsStream(_uiState.value.isOnline)
            is DriverUiEvent.AcceptBooking -> acceptBooking(event.bookingId)
            is DriverUiEvent.RejectBooking -> rejectBooking(event.bookingId)
            is DriverUiEvent.ClearActionMessage -> _uiState.update { it.copy(actionMessage = null) }
            is DriverUiEvent.UpdateRideStatus -> updateRideStatus(event.bookingId, event.newStatus)
            is DriverUiEvent.CompleteBooking -> completeBooking(event.bookingId)
            is DriverUiEvent.CancelBooking -> cancelBooking(event.bookingId, event.reason)
            is DriverUiEvent.ClearRideStatusError -> _uiState.update { it.copy(rideStatusError = null) }
            is DriverUiEvent.OpenRatingSheet -> _uiState.update { it.copy(isRatingSheetVisible = true, ratingError = null) }
            is DriverUiEvent.CloseRatingSheet -> _uiState.update { it.copy(isRatingSheetVisible = false, ratingError = null) }
            is DriverUiEvent.SubmitCustomerRating -> submitCustomerRating(event.rating, event.review)
            is DriverUiEvent.OpenVehicleScreen -> _uiState.update { it.copy(isVehicleScreenVisible = true) }
            is DriverUiEvent.CloseVehicleScreen -> _uiState.update { it.copy(isVehicleScreenVisible = false) }
        }
    }

    private fun submitCustomerRating(rating: Int, review: String?) {
        val state = _uiState.value
        val booking = state.activeDriverBooking ?: return
        val currentUid = authRepository.currentUser?.uid ?: return

        if (state.isSubmittingRating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingRating = true, ratingError = null) }

            val result = ratingRepository.submitRating(
                bookingId = booking.bookingId,
                fromUserId = currentUid,
                toUserId = booking.customerId,
                role = "DRIVER",
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
                            actionMessage = "Customer rating submitted!",
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

    private fun acceptBooking(bookingId: String) {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.acceptingBookingId != null || !state.isOnline) return

        viewModelScope.launch {
            _uiState.update { it.copy(acceptingBookingId = bookingId, actionMessage = null) }

            when (val result = bookingRepository.acceptBooking(bookingId, user.uid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            acceptingBookingId = null,
                            actionMessage = "Ride accepted! Switched to Active Ride.",
                            selectedTab = DriverTab.ACTIVE_RIDE
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            acceptingBookingId = null,
                            actionMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun rejectBooking(bookingId: String) {
        _uiState.update {
            it.copy(
                dismissedBookingIds = it.dismissedBookingIds + bookingId,
                actionMessage = "Ride request dismissed."
            )
        }
    }

    private fun updateRideStatus(bookingId: String, newStatus: BookingStatus) {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.isUpdatingRideStatus) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingRideStatus = true, rideStatusError = null) }

            when (val result = bookingRepository.updateBookingStatus(bookingId, user.uid, newStatus)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingRideStatus = false,
                            rideStatusError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingRideStatus = false,
                            rideStatusError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun completeBooking(bookingId: String) {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.isUpdatingRideStatus) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingRideStatus = true, rideStatusError = null) }

            when (val result = bookingRepository.completeBooking(bookingId, user.uid)) {
                is Resource.Success -> {
                    stopLocationTracking()
                    _uiState.update {
                        it.copy(
                            isUpdatingRideStatus = false,
                            activeDriverBooking = null,
                            activeCustomerName = null,
                            actionMessage = "Ride completed successfully!",
                            selectedTab = DriverTab.DASHBOARD
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingRideStatus = false,
                            rideStatusError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun cancelBooking(bookingId: String, reason: String) {
        val state = _uiState.value
        if (state.isUpdatingRideStatus) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingRideStatus = true, rideStatusError = null) }

            when (val result = bookingRepository.cancelBooking(bookingId, reason)) {
                is Resource.Success -> {
                    stopLocationTracking()
                    _uiState.update {
                        it.copy(
                            isUpdatingRideStatus = false,
                            activeDriverBooking = null,
                            activeCustomerName = null,
                            actionMessage = "Ride cancelled.",
                            selectedTab = DriverTab.DASHBOARD
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingRideStatus = false,
                            rideStatusError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
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
                    syncRequestsStream(targetStatus)
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
            stopLocationTracking()
            val currentUid = authRepository.currentUser?.uid
            if (currentUid != null) {
                notificationRepository.clearFcmToken(currentUid)
            }
            authRepository.signOut()
            onComplete()
        }
    }

    override fun onCleared() {
        stopLocationTracking()
    }
}
