package com.deecode.myapp.feature.admin.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.repository.AdminRepository
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminBookingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBookingsUiState())
    val uiState: StateFlow<AdminBookingsUiState> = _uiState.asStateFlow()

    private var bookingsJob: Job? = null

    init {
        verifyAdminAndLoad()
    }

    private fun verifyAdminAndLoad() {
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Authoritative server-side role check from Firestore
            userRepository.observeUserProfile(authUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val user = resource.data
                        if (user.role != UserRole.ADMIN) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isUnauthorized = true,
                                    errorMessage = "Access Denied: Administrator privileges required."
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isUnauthorized = false) }
                            startBookingsStream()
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
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun startBookingsStream() {
        bookingsJob?.cancel()
        bookingsJob = viewModelScope.launch {
            adminRepository.observeAllBookings().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val list = resource.data
                        _uiState.update { current ->
                            val updatedSelected = current.selectedBooking?.let { selected ->
                                list.find { it.bookingId == selected.bookingId }
                            }
                            current.copy(
                                isLoading = false,
                                allBookings = list,
                                selectedBooking = updatedSelected ?: current.selectedBooking,
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
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun onEvent(event: AdminBookingsUiEvent) {
        when (event) {
            is AdminBookingsUiEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            is AdminBookingsUiEvent.SelectStatusFilter -> _uiState.update { it.copy(statusFilter = event.filter) }
            is AdminBookingsUiEvent.SelectDateFilter -> _uiState.update { it.copy(dateFilter = event.filter) }
            is AdminBookingsUiEvent.SelectBooking -> _uiState.update { it.copy(selectedBooking = event.booking) }
            is AdminBookingsUiEvent.CancelBookingAsAdmin -> cancelBooking(event.bookingId, event.reason)
            is AdminBookingsUiEvent.Refresh -> verifyAdminAndLoad()
            is AdminBookingsUiEvent.ClearActionMessage -> _uiState.update { it.copy(actionMessage = null) }
            is AdminBookingsUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun cancelBooking(bookingId: String, reason: String) {
        val adminUid = authRepository.currentUser?.uid ?: return
        if (_uiState.value.isCancelling) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            when (val result = adminRepository.cancelBookingAsAdmin(bookingId, reason, adminUid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            actionMessage = "Booking #${bookingId.takeLast(8)} cancelled by Admin."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
