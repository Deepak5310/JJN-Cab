package com.deecode.myapp.feature.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.feature.driver.activeride.DriverActiveRideScreen
import com.deecode.myapp.feature.driver.dashboard.DriverDashboardScreen
import com.deecode.myapp.feature.driver.profile.DriverProfileScreen
import com.deecode.myapp.feature.driver.requests.DriverRequestsScreen
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.theme.spacing

@Composable
fun DriverAppShell(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading && uiState.user == null) {
        JJNLoadingIndicator()
        return
    }

    if (uiState.isUnauthorized) {
        // Access Denied Screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔒", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            Text(
                text = "Access Restricted",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                text = "Your account does not have Driver privileges. Please log in with an authorized Driver account.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            JJNOutlinedButton(
                text = "Switch Account / Logout",
                onClick = { viewModel.signOut(onLogout) },
                modifier = Modifier.fillMaxWidth()
            )
        }
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
                    selected = uiState.selectedTab == DriverTab.DASHBOARD,
                    onClick = { viewModel.onEvent(DriverUiEvent.SelectTab(DriverTab.DASHBOARD)) },
                    icon = { Text(text = "🚖") },
                    label = {
                        Text(
                            text = "Dashboard",
                            fontWeight = if (uiState.selectedTab == DriverTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == DriverTab.REQUESTS,
                    onClick = { viewModel.onEvent(DriverUiEvent.SelectTab(DriverTab.REQUESTS)) },
                    icon = { Text(text = "📡") },
                    label = {
                        Text(
                            text = "Requests",
                            fontWeight = if (uiState.selectedTab == DriverTab.REQUESTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == DriverTab.ACTIVE_RIDE,
                    onClick = { viewModel.onEvent(DriverUiEvent.SelectTab(DriverTab.ACTIVE_RIDE)) },
                    icon = { Text(text = "🏎️") },
                    label = {
                        Text(
                            text = "Active",
                            fontWeight = if (uiState.selectedTab == DriverTab.ACTIVE_RIDE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == DriverTab.PROFILE,
                    onClick = { viewModel.onEvent(DriverUiEvent.SelectTab(DriverTab.PROFILE)) },
                    icon = { Text(text = "👤") },
                    label = {
                        Text(
                            text = "Profile",
                            fontWeight = if (uiState.selectedTab == DriverTab.PROFILE) FontWeight.Bold else FontWeight.Normal
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
                DriverTab.DASHBOARD -> {
                    DriverDashboardScreen(
                        user = uiState.user,
                        isOnline = uiState.isOnline,
                        isUpdatingAvailability = uiState.isUpdatingAvailability,
                        availabilityError = uiState.availabilityError,
                        onToggleOnlineStatus = { viewModel.onEvent(DriverUiEvent.ToggleOnlineStatus) },
                        onClearAvailabilityError = { viewModel.onEvent(DriverUiEvent.ClearAvailabilityError) }
                    )
                }
                DriverTab.REQUESTS -> {
                    DriverRequestsScreen(
                        isOnline = uiState.isOnline,
                        pendingBookings = uiState.pendingBookings,
                        dismissedBookingIds = uiState.dismissedBookingIds,
                        acceptingBookingId = uiState.acceptingBookingId,
                        actionMessage = uiState.actionMessage,
                        isLoading = uiState.isLoadingRequests,
                        errorMessage = uiState.requestsError,
                        onAcceptBooking = { bookingId -> viewModel.onEvent(DriverUiEvent.AcceptBooking(bookingId)) },
                        onRejectBooking = { bookingId -> viewModel.onEvent(DriverUiEvent.RejectBooking(bookingId)) },
                        onClearActionMessage = { viewModel.onEvent(DriverUiEvent.ClearActionMessage) },
                        onRefresh = { viewModel.onEvent(DriverUiEvent.RefreshRequests) }
                    )
                }
                DriverTab.ACTIVE_RIDE -> {
                    DriverActiveRideScreen(
                        activeBooking = uiState.activeDriverBooking,
                        customerName = uiState.activeCustomerName,
                        isUpdatingStatus = uiState.isUpdatingRideStatus,
                        rideStatusError = uiState.rideStatusError,
                        onUpdateStatus = { newStatus ->
                            uiState.activeDriverBooking?.let { booking ->
                                viewModel.onEvent(DriverUiEvent.UpdateRideStatus(booking.bookingId, newStatus))
                            }
                        },
                        onCompleteRide = {
                            uiState.activeDriverBooking?.let { booking ->
                                viewModel.onEvent(DriverUiEvent.CompleteBooking(booking.bookingId))
                            }
                        },
                        onCancelRide = { reason ->
                            uiState.activeDriverBooking?.let { booking ->
                                viewModel.onEvent(DriverUiEvent.CancelBooking(booking.bookingId, reason))
                            }
                        },
                        onClearError = { viewModel.onEvent(DriverUiEvent.ClearRideStatusError) }
                    )
                }
                DriverTab.PROFILE -> {
                    DriverProfileScreen(
                        user = uiState.user,
                        onLogout = { viewModel.signOut(onLogout) }
                    )
                }
            }
        }
    }
}
