package com.deecode.myapp.feature.driver.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.BookingRepository
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
class DriverProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverProfileUiState())
    val uiState: StateFlow<DriverProfileUiState> = _uiState.asStateFlow()

    private val phoneRegex = Regex("^[+]?[0-9]{10,13}$")

    private var profileJob: Job? = null
    private var bookingsJob: Job? = null
    private var vehicleJob: Job? = null

    init {
        loadDriverProfile()
    }

    private fun loadDriverProfile() {
        val currentUid = authRepository.currentUser?.uid
        if (currentUid == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User session expired.") }
            return
        }

        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            userRepository.observeUserProfile(currentUid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val user = resource.data
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                user = user,
                                editName = if (!state.isEditing) user.name else state.editName,
                                editPhone = if (!state.isEditing) user.phone else state.editPhone,
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
            bookingRepository.observeDriverBookings(currentUid).collect { resource ->
                if (resource is Resource.Success) {
                    val count = resource.data.count { it.status == BookingStatus.COMPLETED }
                    _uiState.update { it.copy(completedRides = count) }
                }
            }
        }

        vehicleJob?.cancel()
        vehicleJob = viewModelScope.launch {
            vehicleRepository.observeDriverVehicle(currentUid).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(vehicle = resource.data) }
                }
            }
        }
    }

    fun onEvent(event: DriverProfileUiEvent) {
        when (event) {
            is DriverProfileUiEvent.StartEditing -> {
                _uiState.update {
                    it.copy(
                        isEditing = true,
                        editName = it.user?.name ?: "",
                        editPhone = it.user?.phone ?: "",
                        nameError = null,
                        phoneError = null,
                        successMessage = null
                    )
                }
            }
            is DriverProfileUiEvent.CancelEditing -> {
                _uiState.update {
                    it.copy(
                        isEditing = false,
                        nameError = null,
                        phoneError = null
                    )
                }
            }
            is DriverProfileUiEvent.NameChanged -> {
                _uiState.update {
                    it.copy(
                        editName = event.name,
                        nameError = if (event.name.trim().length < 2) "Name must be at least 2 characters" else null
                    )
                }
            }
            is DriverProfileUiEvent.PhoneChanged -> {
                val cleaned = event.phone.trim()
                _uiState.update {
                    it.copy(
                        editPhone = event.phone,
                        phoneError = if (cleaned.isNotEmpty() && !phoneRegex.matches(cleaned)) "Enter a valid phone number (10-13 digits)" else null
                    )
                }
            }
            is DriverProfileUiEvent.SaveProfile -> saveProfile()
            is DriverProfileUiEvent.ClearSuccessMessage -> _uiState.update { it.copy(successMessage = null) }
            is DriverProfileUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun saveProfile() {
        val state = _uiState.value
        val currentUid = authRepository.currentUser?.uid ?: return

        val name = state.editName.trim()
        val phone = state.editPhone.trim()

        val nameError = if (name.length < 2) "Name must be at least 2 characters" else null
        val phoneError = when {
            phone.isBlank() -> "Phone number is required"
            !phoneRegex.matches(phone) -> "Enter a valid phone number (10-13 digits)"
            else -> null
        }

        if (nameError != null || phoneError != null) {
            _uiState.update { it.copy(nameError = nameError, phoneError = phoneError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            when (val result = userRepository.updateProfile(currentUid, name, phone)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            successMessage = "Driver profile updated successfully! ✅",
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        profileJob?.cancel()
        bookingsJob?.cancel()
        vehicleJob?.cancel()
    }
}
