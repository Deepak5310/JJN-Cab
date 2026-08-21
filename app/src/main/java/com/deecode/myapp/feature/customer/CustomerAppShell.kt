package com.deecode.myapp.feature.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.feature.customer.bookings.CustomerBookingsScreen
import com.deecode.myapp.feature.customer.home.CustomerHomeScreen
import com.deecode.myapp.feature.customer.profile.CustomerProfileScreen
import com.deecode.myapp.ui.components.JJNLoadingIndicator

@Composable
fun CustomerAppShell(
    onLogout: () -> Unit,
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
                        onNavigateToBookings = {
                            viewModel.onEvent(CustomerUiEvent.SelectTab(CustomerTab.BOOKINGS))
                        }
                    )
                }
                CustomerTab.BOOKINGS -> {
                    CustomerBookingsScreen(
                        onBookRideClick = {
                            viewModel.onEvent(CustomerUiEvent.SelectTab(CustomerTab.HOME))
                        }
                    )
                }
                CustomerTab.PROFILE -> {
                    CustomerProfileScreen(
                        user = uiState.user,
                        onLogout = { viewModel.signOut(onLogout) }
                    )
                }
            }
        }
    }
}
