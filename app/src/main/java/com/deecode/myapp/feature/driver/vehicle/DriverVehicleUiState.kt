package com.deecode.myapp.feature.driver.vehicle

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.Vehicle

data class DriverVehicleUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val vehicleType: String = "SEDAN",
    val makeModel: String = "",
    val registrationNumber: String = "",
    val color: String = "",
    val existingVehicle: Vehicle? = null,
    val typeError: String? = null,
    val makeModelError: String? = null,
    val registrationError: String? = null,
    val colorError: String? = null,
    val isSavedSuccessfully: Boolean = false,
    val errorMessage: String? = null
) : UiState {
    val isValid: Boolean
        get() = typeError == null &&
                makeModelError == null &&
                registrationError == null &&
                colorError == null &&
                makeModel.isNotBlank() &&
                registrationNumber.isNotBlank() &&
                color.isNotBlank()
}

sealed interface DriverVehicleUiEvent : UiEvent {
    data class VehicleTypeChanged(val type: String) : DriverVehicleUiEvent
    data class MakeModelChanged(val makeModel: String) : DriverVehicleUiEvent
    data class RegistrationNumberChanged(val regNumber: String) : DriverVehicleUiEvent
    data class ColorChanged(val color: String) : DriverVehicleUiEvent
    data object SaveVehicle : DriverVehicleUiEvent
    data object ClearSuccess : DriverVehicleUiEvent
    data object ClearError : DriverVehicleUiEvent
}
