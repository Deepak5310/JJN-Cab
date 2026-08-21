package com.deecode.myapp.feature.driver.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.Vehicle
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverVehicleViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverVehicleUiState())
    val uiState: StateFlow<DriverVehicleUiState> = _uiState.asStateFlow()

    private val plateRegex = Regex("^[A-Z]{2}[ -]?[0-9]{1,2}[ -]?[A-Z]{1,3}[ -]?[0-9]{4}$", RegexOption.IGNORE_CASE)

    init {
        loadDriverVehicle()
    }

    private fun loadDriverVehicle() {
        val currentUid = authRepository.currentUser?.uid
        if (currentUid == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User session expired.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            vehicleRepository.observeDriverVehicle(currentUid).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val vehicle = resource.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                existingVehicle = vehicle,
                                vehicleType = vehicle?.vehicleType ?: it.vehicleType,
                                makeModel = if (it.makeModel.isBlank()) vehicle?.makeModel ?: "" else it.makeModel,
                                registrationNumber = if (it.registrationNumber.isBlank()) vehicle?.registrationNumber ?: "" else it.registrationNumber,
                                color = if (it.color.isBlank()) vehicle?.color ?: "" else it.color,
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

    fun onEvent(event: DriverVehicleUiEvent) {
        when (event) {
            is DriverVehicleUiEvent.VehicleTypeChanged -> {
                _uiState.update { it.copy(vehicleType = event.type, typeError = null) }
            }
            is DriverVehicleUiEvent.MakeModelChanged -> {
                _uiState.update {
                    it.copy(
                        makeModel = event.makeModel,
                        makeModelError = if (event.makeModel.isBlank()) "Vehicle make & model is required." else null
                    )
                }
            }
            is DriverVehicleUiEvent.RegistrationNumberChanged -> {
                val formatted = event.regNumber.uppercase()
                _uiState.update {
                    val error = when {
                        formatted.isBlank() -> "Registration number is required."
                        !plateRegex.matches(formatted.trim()) -> "Invalid plate format (e.g. DL 01 AB 1234)"
                        else -> null
                    }
                    it.copy(registrationNumber = formatted, registrationError = error)
                }
            }
            is DriverVehicleUiEvent.ColorChanged -> {
                _uiState.update {
                    it.copy(
                        color = event.color,
                        colorError = if (event.color.isBlank()) "Vehicle color is required." else null
                    )
                }
            }
            is DriverVehicleUiEvent.SaveVehicle -> saveVehicle()
            is DriverVehicleUiEvent.ClearSuccess -> _uiState.update { it.copy(isSavedSuccessfully = false) }
            is DriverVehicleUiEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun saveVehicle() {
        val state = _uiState.value
        val currentUid = authRepository.currentUser?.uid ?: return

        val makeModelError = if (state.makeModel.isBlank()) "Vehicle make & model is required." else null
        val regError = when {
            state.registrationNumber.isBlank() -> "Registration number is required."
            !plateRegex.matches(state.registrationNumber.trim()) -> "Invalid plate format (e.g. DL 01 AB 1234)"
            else -> null
        }
        val colorError = if (state.color.isBlank()) "Vehicle color is required." else null

        if (makeModelError != null || regError != null || colorError != null) {
            _uiState.update {
                it.copy(
                    makeModelError = makeModelError,
                    registrationError = regError,
                    colorError = colorError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val vehicle = Vehicle(
                driverId = currentUid,
                vehicleType = state.vehicleType,
                makeModel = state.makeModel.trim(),
                registrationNumber = state.registrationNumber.trim().uppercase(),
                color = state.color.trim()
            )

            when (val result = vehicleRepository.saveVehicle(vehicle)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSavedSuccessfully = true,
                            existingVehicle = vehicle,
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
}
