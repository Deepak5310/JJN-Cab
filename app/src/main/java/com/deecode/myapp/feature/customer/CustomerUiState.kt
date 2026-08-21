package com.deecode.myapp.feature.customer

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.LocationPoint
import com.deecode.myapp.domain.model.PlaceSuggestion
import com.deecode.myapp.domain.model.User

enum class CustomerTab {
    HOME,
    BOOKINGS,
    PROFILE
}

enum class LocationTarget {
    PICKUP,
    DESTINATION
}

data class CustomerUiState(
    val user: User? = null,
    val selectedTab: CustomerTab = CustomerTab.HOME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentLocation: LocationPoint? = null,
    val isLocating: Boolean = false,
    val locationError: String? = null,
    val isPermissionPermanentlyDenied: Boolean = false,

    // Pickup & Destination Selection
    val pickupLocation: LocationPoint? = null,
    val destinationLocation: LocationPoint? = null,
    val searchQuery: String = "",
    val isSearchingPlaces: Boolean = false,
    val placeSuggestions: List<PlaceSuggestion> = emptyList(),
    val isSearchBottomSheetVisible: Boolean = false,
    val activeLocationTarget: LocationTarget = LocationTarget.DESTINATION,
    val isSelectingOnMap: Boolean = false,
    val isReverseGeocoding: Boolean = false
) : UiState

sealed interface CustomerUiEvent : UiEvent {
    data class SelectTab(val tab: CustomerTab) : CustomerUiEvent
    data object Refresh : CustomerUiEvent
    data object ClearError : CustomerUiEvent
    data object RequestLocation : CustomerUiEvent
    data class OnLocationPermissionDenied(val permanentlyDenied: Boolean) : CustomerUiEvent
    data object ClearLocationError : CustomerUiEvent

    // Place Search & Selection
    data class OpenPlaceSearch(val target: LocationTarget) : CustomerUiEvent
    data object ClosePlaceSearch : CustomerUiEvent
    data class UpdateSearchQuery(val query: String) : CustomerUiEvent
    data class SelectPlaceSuggestion(val suggestion: PlaceSuggestion) : CustomerUiEvent
    data class ClearSelectedLocation(val target: LocationTarget) : CustomerUiEvent
    data class StartMapSelection(val target: LocationTarget) : CustomerUiEvent
    data class ConfirmMapSelection(val latitude: Double, val longitude: Double) : CustomerUiEvent
    data object CancelMapSelection : CustomerUiEvent
}
