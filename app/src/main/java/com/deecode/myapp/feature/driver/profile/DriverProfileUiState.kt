package com.deecode.myapp.feature.driver.profile

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.domain.model.Vehicle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriverProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val vehicle: Vehicle? = null,
    val completedRides: Int = 0,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editPhone: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) : UiState {
    val initials: String
        get() {
            val letters = user?.name?.trim()?.split(Regex("\\s+"))
                ?.mapNotNull { it.firstOrNull()?.toString() }
                ?.take(2)
                ?.joinToString("")
                ?.uppercase()
            return if (letters.isNullOrBlank()) "D" else letters
        }

    val formattedJoinedDate: String
        get() {
            val time = user?.createdAt ?: 0L
            if (time <= 0L) return "Recent"
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(time))
        }

    val isValid: Boolean
        get() = nameError == null &&
                phoneError == null &&
                editName.trim().length >= 2 &&
                editPhone.trim().isNotBlank()
}

sealed interface DriverProfileUiEvent : UiEvent {
    data object StartEditing : DriverProfileUiEvent
    data object CancelEditing : DriverProfileUiEvent
    data class NameChanged(val name: String) : DriverProfileUiEvent
    data class PhoneChanged(val phone: String) : DriverProfileUiEvent
    data object SaveProfile : DriverProfileUiEvent
    data object ClearSuccessMessage : DriverProfileUiEvent
    data object ClearError : DriverProfileUiEvent
}
