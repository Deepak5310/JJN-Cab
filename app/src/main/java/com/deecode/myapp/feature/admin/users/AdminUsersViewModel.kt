package com.deecode.myapp.feature.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.User
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
class AdminUsersViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    private var usersJob: Job? = null

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
                            startUsersStream()
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

    private fun startUsersStream() {
        usersJob?.cancel()
        usersJob = viewModelScope.launch {
            adminRepository.observeUsers().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val list = resource.data
                        _uiState.update { current ->
                            val updatedSelected = current.selectedUser?.let { selected ->
                                list.find { it.uid == selected.uid }
                            }
                            current.copy(
                                isLoading = false,
                                allUsers = list,
                                selectedUser = updatedSelected ?: current.selectedUser,
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

    fun onEvent(event: AdminUsersUiEvent) {
        when (event) {
            is AdminUsersUiEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            is AdminUsersUiEvent.SelectFilter -> _uiState.update { it.copy(selectedFilter = event.filter) }
            is AdminUsersUiEvent.SelectUser -> _uiState.update { it.copy(selectedUser = event.user) }
            is AdminUsersUiEvent.ToggleUserActive -> toggleUserActive(event.targetUid, event.isActive)
            is AdminUsersUiEvent.ToggleDriverVerification -> toggleDriverVerification(event.targetUid, event.isVerified)
            is AdminUsersUiEvent.ChangeUserRole -> changeUserRole(event.targetUid, event.newRole)
            is AdminUsersUiEvent.Refresh -> verifyAdminAndLoad()
            is AdminUsersUiEvent.ClearActionMessage -> _uiState.update { it.copy(actionMessage = null) }
            is AdminUsersUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun toggleUserActive(targetUid: String, isActive: Boolean) {
        val adminUid = authRepository.currentUser?.uid ?: return
        if (_uiState.value.isUpdating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = adminRepository.setUserActiveStatus(targetUid, isActive, adminUid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            actionMessage = if (isActive) "User access enabled" else "User access suspended"
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

    private fun toggleDriverVerification(targetUid: String, isVerified: Boolean) {
        val adminUid = authRepository.currentUser?.uid ?: return
        if (_uiState.value.isUpdating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = adminRepository.setDriverVerification(targetUid, isVerified, adminUid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            actionMessage = if (isVerified) "Driver partner verified ✅" else "Driver verification revoked"
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

    private fun changeUserRole(targetUid: String, newRole: UserRole) {
        val adminUid = authRepository.currentUser?.uid ?: return
        if (_uiState.value.isUpdating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = adminRepository.setUserRole(targetUid, newRole, adminUid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            actionMessage = "Account type changed to ${newRole.name}"
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
