package com.deecode.myapp.feature.driver.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DriverHistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHistoryUiState())
    val uiState: StateFlow<DriverHistoryUiState> = _uiState.asStateFlow()

    private var historyJob: Job? = null

    init {
        observeDriverHistory()
    }

    private fun observeDriverHistory() {
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

        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            bookingRepository.observeDriverBookings(currentUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val allBookings = resource.data
                        val startOfDay = getStartOfDayMillis()

                        val completedRides = allBookings.filter { it.status == BookingStatus.COMPLETED }
                        val cancelledRides = allBookings.filter {
                            it.status in setOf(
                                BookingStatus.CANCELLED,
                                BookingStatus.CANCELLED_BY_CUSTOMER,
                                BookingStatus.CANCELLED_BY_DRIVER
                            )
                        }

                        val totalEarnings = completedRides.sumOf { it.finalFare ?: it.estimatedFare }
                        val todayEarnings = completedRides.filter {
                            val timestamp = it.completedAt?.takeIf { ts -> ts > 0L } ?: it.createdAt
                            timestamp >= startOfDay
                        }.sumOf { it.finalFare ?: it.estimatedFare }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                bookings = allBookings,
                                todayEarnings = todayEarnings,
                                totalEarnings = totalEarnings,
                                completedCount = completedRides.size,
                                cancelledCount = cancelledRides.size,
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

    private fun getStartOfDayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun onEvent(event: DriverHistoryUiEvent) {
        when (event) {
            is DriverHistoryUiEvent.Refresh -> observeDriverHistory()
            is DriverHistoryUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }
}
