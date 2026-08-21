package com.deecode.myapp.feature.customer.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerBookingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerBookingsUiState())
    val uiState: StateFlow<CustomerBookingsUiState> = _uiState.asStateFlow()

    private var bookingsJob: Job? = null

    init {
        observeBookings()
    }

    private fun observeBookings() {
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

        bookingsJob?.cancel()
        bookingsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingRepository.observeCustomerBookings(currentUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                bookings = resource.data,
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

    fun onEvent(event: CustomerBookingsUiEvent) {
        when (event) {
            is CustomerBookingsUiEvent.Refresh -> observeBookings()
            is CustomerBookingsUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }
}
