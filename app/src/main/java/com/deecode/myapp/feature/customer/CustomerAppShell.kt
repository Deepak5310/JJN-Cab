package com.deecode.myapp.feature.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.feature.customer.bookings.CustomerBookingsScreen
import com.deecode.myapp.feature.customer.home.CustomerHomeScreen
import com.deecode.myapp.feature.customer.profile.CustomerProfileScreen
import com.deecode.myapp.ui.components.JJNLoadingIndicator

@Composable
fun CustomerAppShell(
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToActiveBooking: () -> Unit = {},
    onNavigateToRideDetails: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading && uiState.user == null) {
        JJNLoadingIndicator()
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == CustomerTab.HOME,
                    onClick = { viewModel.onEvent(CustomerUiEvent.SelectTab(CustomerTab.HOME)) },
                    icon = { Text(text = "🚕") },
                    label = {
                        Text(
                            text = "Home",
                            fontWeight = if (uiState.selectedTab == CustomerTab.HOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == CustomerTab.BOOKINGS,
                    onClick = { viewModel.onEvent(CustomerUiEvent.SelectTab(CustomerTab.BOOKINGS)) },
                    icon = { Text(text = "📋") },
                    label = {
                        Text(
                            text = "Bookings",
                            fontWeight = if (uiState.selectedTab == CustomerTab.BOOKINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == CustomerTab.PROFILE,
                    onClick = { viewModel.onEvent(CustomerUiEvent.SelectTab(CustomerTab.PROFILE)) },
                    icon = { Text(text = "👤") },
                    label = {
                        Text(
                            text = "Profile",
                            fontWeight = if (uiState.selectedTab == CustomerTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                CustomerTab.HOME -> {
                    CustomerHomeScreen(
                        user = uiState.user,
                        currentLocation = uiState.currentLocation,
                        pickupLocation = uiState.pickupLocation,
                        destinationLocation = uiState.destinationLocation,
                        routeInfo = uiState.routeInfo,
                        isCalculatingRoute = uiState.isCalculatingRoute,
                        routeError = uiState.routeError,
                        selectedRideTier = uiState.selectedRideTier,
                        fareEstimates = uiState.fareEstimates,
                        isConfirmBookingSheetVisible = uiState.isConfirmBookingSheetVisible,
                        isCreatingBooking = uiState.isCreatingBooking,
                        createdBooking = uiState.createdBooking,
                        bookingCreationError = uiState.bookingCreationError,
                        hasLocationPermission = viewModel.hasLocationPermission(),
                        isLocating = uiState.isLocating,
                        locationError = uiState.locationError,
                        isPermissionPermanentlyDenied = uiState.isPermissionPermanentlyDenied,
                        isSearchBottomSheetVisible = uiState.isSearchBottomSheetVisible,
                        activeLocationTarget = uiState.activeLocationTarget,
                        searchQuery = uiState.searchQuery,
                        isSearchingPlaces = uiState.isSearchingPlaces,
                        placeSuggestions = uiState.placeSuggestions,
                        isSelectingOnMap = uiState.isSelectingOnMap,
                        isReverseGeocoding = uiState.isReverseGeocoding,
                        onRequestLocation = {
                            viewModel.onEvent(CustomerUiEvent.RequestLocation)
                        },
                        onPermissionDenied = { permanentlyDenied ->
                            viewModel.onEvent(CustomerUiEvent.OnLocationPermissionDenied(permanentlyDenied))
                        },
                        onClearLocationError = {
                            viewModel.onEvent(CustomerUiEvent.ClearLocationError)
                        },
                        onOpenPlaceSearch = { target ->
                            viewModel.onEvent(CustomerUiEvent.OpenPlaceSearch(target))
                        },
                        onClosePlaceSearch = {
                            viewModel.onEvent(CustomerUiEvent.ClosePlaceSearch)
                        },
                        onUpdateSearchQuery = { query ->
                            viewModel.onEvent(CustomerUiEvent.UpdateSearchQuery(query))
                        },
                        onSelectPlaceSuggestion = { suggestion ->
                            viewModel.onEvent(CustomerUiEvent.SelectPlaceSuggestion(suggestion))
                        },
                        onClearSelectedLocation = { target ->
                            viewModel.onEvent(CustomerUiEvent.ClearSelectedLocation(target))
                        },
                        onStartMapSelection = { target ->
                            viewModel.onEvent(CustomerUiEvent.StartMapSelection(target))
                        },
                        onConfirmMapSelection = { lat, lng ->
                            viewModel.onEvent(CustomerUiEvent.ConfirmMapSelection(lat, lng))
                        },
                        onCancelMapSelection = {
                            viewModel.onEvent(CustomerUiEvent.CancelMapSelection)
                        },
                        onSelectRideTier = { tier ->
                            viewModel.onEvent(CustomerUiEvent.SelectRideTier(tier))
                        },
                        activeBooking = uiState.activeBooking,
                        onOpenConfirmBooking = {
                            viewModel.onEvent(CustomerUiEvent.OpenConfirmBooking)
                        },
                        onCloseConfirmBooking = {
                            viewModel.onEvent(CustomerUiEvent.CloseConfirmBooking)
                        },
                        onSubmitBooking = {
                            viewModel.onEvent(CustomerUiEvent.SubmitBooking)
                        },
                        onClearCreatedBooking = {
                            viewModel.onEvent(CustomerUiEvent.ClearCreatedBooking)
                        },
                        onNavigateToActiveBooking = onNavigateToActiveBooking,
                        onNavigateToBookings = {
                            viewModel.onEvent(CustomerUiEvent.SelectTab(CustomerTab.BOOKINGS))
                        }
                    )
                }
                CustomerTab.BOOKINGS -> {
                    CustomerBookingsScreen(
                        onBookRideClick = {
                            viewModel.onEvent(CustomerUiEvent.SelectTab(CustomerTab.HOME))
                        },
                        onBookingClick = onNavigateToRideDetails
                    )
                }
                CustomerTab.PROFILE -> {
                    CustomerProfileScreen(
                        onNavigateToSettings = onNavigateToSettings,
                        onLogout = { viewModel.signOut(onLogout) }
                    )
                }
            }
        }
    }
}
