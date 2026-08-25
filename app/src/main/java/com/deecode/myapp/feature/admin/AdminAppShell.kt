package com.deecode.myapp.feature.admin

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.feature.admin.bookings.AdminBookingsScreen
import com.deecode.myapp.feature.admin.dashboard.AdminDashboardScreen
import com.deecode.myapp.feature.admin.drivers.AdminDriversScreen
import com.deecode.myapp.feature.admin.profile.AdminProfileScreen
import com.deecode.myapp.feature.admin.users.AdminUsersScreen
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.theme.spacing

@Composable
fun AdminAppShell(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    viewModel: AdminViewModel = hiltViewModel()
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
                text = "Your account does not have Administrator privileges. Please log in with an authorized Admin account.",
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
                    selected = uiState.selectedTab == AdminTab.DASHBOARD,
                    onClick = { viewModel.onEvent(AdminUiEvent.SelectTab(AdminTab.DASHBOARD)) },
                    icon = { Text(text = "📊") },
                    label = {
                        Text(
                            text = "Dashboard",
                            fontWeight = if (uiState.selectedTab == AdminTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.BOOKINGS,
                    onClick = { viewModel.onEvent(AdminUiEvent.SelectTab(AdminTab.BOOKINGS)) },
                    icon = { Text(text = "📋") },
                    label = {
                        Text(
                            text = "Bookings",
                            fontWeight = if (uiState.selectedTab == AdminTab.BOOKINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.DRIVERS,
                    onClick = { viewModel.onEvent(AdminUiEvent.SelectTab(AdminTab.DRIVERS)) },
                    icon = { Text(text = "🚕") },
                    label = {
                        Text(
                            text = "Drivers",
                            fontWeight = if (uiState.selectedTab == AdminTab.DRIVERS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.USERS,
                    onClick = { viewModel.onEvent(AdminUiEvent.SelectTab(AdminTab.USERS)) },
                    icon = { Text(text = "👥") },
                    label = {
                        Text(
                            text = "Users",
                            fontWeight = if (uiState.selectedTab == AdminTab.USERS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.PROFILE,
                    onClick = { viewModel.onEvent(AdminUiEvent.SelectTab(AdminTab.PROFILE)) },
                    icon = { Text(text = "👤") },
                    label = {
                        Text(
                            text = "Profile",
                            fontWeight = if (uiState.selectedTab == AdminTab.PROFILE) FontWeight.Bold else FontWeight.Normal
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
                AdminTab.DASHBOARD -> {
                    AdminDashboardScreen(user = uiState.user)
                }
                AdminTab.BOOKINGS -> {
                    AdminBookingsScreen()
                }
                AdminTab.DRIVERS -> {
                    AdminDriversScreen()
                }
                AdminTab.USERS -> {
                    AdminUsersScreen()
                }
                AdminTab.PROFILE -> {
                    AdminProfileScreen(
                        user = uiState.user,
                        onNavigateToSettings = onNavigateToSettings,
                        onLogout = { viewModel.signOut(onLogout) }
                    )
                }
            }
        }
    }
}
