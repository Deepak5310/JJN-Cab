package com.deecode.myapp.feature.admin.drivers

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.DriverManagementItem

enum class DriverStatusFilter {
    ALL,
    ONLINE,
    OFFLINE,
    PENDING_VERIFICATION
}

data class AdminDriversUiState(
    val isLoading: Boolean = true,
    val isUnauthorized: Boolean = false,
    val allDrivers: List<DriverManagementItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: DriverStatusFilter = DriverStatusFilter.ALL,
    val selectedDriver: DriverManagementItem? = null,
    val isUpdating: Boolean = false,
    val actionMessage: String? = null,
    val errorMessage: String? = null
) : UiState {
    val filteredDrivers: List<DriverManagementItem>
        get() {
            return allDrivers.filter { driver ->
                val matchesFilter = when (selectedFilter) {
                    DriverStatusFilter.ALL -> true
                    DriverStatusFilter.ONLINE -> driver.isOnline
                    DriverStatusFilter.OFFLINE -> !driver.isOnline
                    DriverStatusFilter.PENDING_VERIFICATION -> !driver.user.isDriverVerified
                }

                val query = searchQuery.trim().lowercase()
                val matchesQuery = query.isBlank() ||
                        driver.user.name.lowercase().contains(query) ||
                        driver.user.email.lowercase().contains(query) ||
                        driver.user.phone.lowercase().contains(query) ||
                        driver.vehiclePlate.lowercase().contains(query) ||
                        driver.vehicleModel.lowercase().contains(query) ||
                        driver.driverId.lowercase().contains(query)

                matchesFilter && matchesQuery
            }
        }
}

sealed interface AdminDriversUiEvent : UiEvent {
    data class SearchQueryChanged(val query: String) : AdminDriversUiEvent
    data class SelectFilter(val filter: DriverStatusFilter) : AdminDriversUiEvent
    data class SelectDriver(val driver: DriverManagementItem?) : AdminDriversUiEvent
    data class ToggleDriverActive(val driverId: String, val isActive: Boolean) : AdminDriversUiEvent
    data class ToggleDriverApproval(val driverId: String, val isApproved: Boolean) : AdminDriversUiEvent
    data object Refresh : AdminDriversUiEvent
    data object ClearActionMessage : AdminDriversUiEvent
    data object ClearError : AdminDriversUiEvent
}
