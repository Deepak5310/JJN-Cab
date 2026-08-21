package com.deecode.myapp.feature.admin.dashboard

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
class AdminDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private var statsJob: Job? = null
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
                            startDashboardStreams()
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

    private fun startDashboardStreams() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            adminRepository.observeDashboardStats().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                stats = resource.data,
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

        bookingsJob?.cancel()
        bookingsJob = viewModelScope.launch {
            adminRepository.observeRecentBookings(10).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(recentBookings = resource.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = resource.message) }
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun onEvent(event: AdminDashboardUiEvent) {
        when (event) {
            is AdminDashboardUiEvent.Refresh -> verifyAdminAndLoad()
            is AdminDashboardUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }
}
