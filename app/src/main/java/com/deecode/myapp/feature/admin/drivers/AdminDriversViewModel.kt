package com.deecode.myapp.feature.admin.drivers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.DriverManagementItem
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
class AdminDriversViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDriversUiState())
    val uiState: StateFlow<AdminDriversUiState> = _uiState.asStateFlow()

    private var driversJob: Job? = null

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
                            startDriversStream()
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

    private fun startDriversStream() {
        driversJob?.cancel()
        driversJob = viewModelScope.launch {
            adminRepository.observeDriverManagementList().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val list = resource.data
                        _uiState.update { current ->
                            val updatedSelected = current.selectedDriver?.let { selected ->
                                list.find { it.driverId == selected.driverId }
                            }
                            current.copy(
                                isLoading = false,
                                allDrivers = list,
                                selectedDriver = updatedSelected ?: current.selectedDriver,
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

    fun onEvent(event: AdminDriversUiEvent) {
        when (event) {
            is AdminDriversUiEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            is AdminDriversUiEvent.SelectFilter -> _uiState.update { it.copy(selectedFilter = event.filter) }
            is AdminDriversUiEvent.SelectDriver -> _uiState.update { it.copy(selectedDriver = event.driver) }
            is AdminDriversUiEvent.ToggleDriverActive -> toggleDriverActive(event.driverId, event.isActive)
            is AdminDriversUiEvent.ToggleDriverApproval -> toggleDriverApproval(event.driverId, event.isApproved)
            is AdminDriversUiEvent.Refresh -> verifyAdminAndLoad()
            is AdminDriversUiEvent.ClearActionMessage -> _uiState.update { it.copy(actionMessage = null) }
            is AdminDriversUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun toggleDriverActive(driverId: String, isActive: Boolean) {
        val adminUid = authRepository.currentUser?.uid ?: return
        if (_uiState.value.isUpdating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = adminRepository.setDriverActiveStatus(driverId, isActive, adminUid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            actionMessage = if (isActive) "Driver access enabled" else "Driver access suspended"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun toggleDriverApproval(driverId: String, isApproved: Boolean) {
        val adminUid = authRepository.currentUser?.uid ?: return
        if (_uiState.value.isUpdating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = adminRepository.setDriverApprovalStatus(driverId, isApproved, adminUid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            actionMessage = if (isApproved) "Driver partner approved ✅" else "Driver partner approval revoked"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
