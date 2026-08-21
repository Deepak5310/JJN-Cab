package com.deecode.myapp.feature.admin.drivers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.domain.model.DriverManagementItem
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.theme.spacing
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminDriversScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDriversViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading && uiState.allDrivers.isEmpty()) {
        JJNLoadingIndicator(modifier = modifier.fillMaxSize())
        return
    }

    if (uiState.isUnauthorized) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.large),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Access Restricted: Administrator privileges required.",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.error)
            )
        }
        return
    }

    val onlineCount = uiState.allDrivers.count { it.isOnline }
    val offlineCount = uiState.allDrivers.count { !it.isOnline }
    val pendingCount = uiState.allDrivers.count { !it.user.isDriverVerified }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Driver Fleet 🚕",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${uiState.allDrivers.size} registered driver partners",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onEvent(AdminDriversUiEvent.SearchQueryChanged(it)) },
            label = { Text("Search by name, phone, plate...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { viewModel.onEvent(AdminDriversUiEvent.SearchQueryChanged("")) }
                            .padding(8.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            FilterChip(
                selected = uiState.selectedFilter == DriverStatusFilter.ALL,
                onClick = { viewModel.onEvent(AdminDriversUiEvent.SelectFilter(DriverStatusFilter.ALL)) },
                label = { Text("All (${uiState.allDrivers.size})") }
            )

            FilterChip(
                selected = uiState.selectedFilter == DriverStatusFilter.ONLINE,
                onClick = { viewModel.onEvent(AdminDriversUiEvent.SelectFilter(DriverStatusFilter.ONLINE)) },
                label = { Text("Online 🟢 ($onlineCount)") }
            )

            FilterChip(
                selected = uiState.selectedFilter == DriverStatusFilter.OFFLINE,
                onClick = { viewModel.onEvent(AdminDriversUiEvent.SelectFilter(DriverStatusFilter.OFFLINE)) },
                label = { Text("Offline ($offlineCount)") }
            )

            FilterChip(
                selected = uiState.selectedFilter == DriverStatusFilter.PENDING_VERIFICATION,
                onClick = { viewModel.onEvent(AdminDriversUiEvent.SelectFilter(DriverStatusFilter.PENDING_VERIFICATION)) },
                label = { Text("Pending ⚠️ ($pendingCount)") }
            )
        }

        // Action / Error Message Banners
        if (!uiState.actionMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                contentPadding = MaterialTheme.spacing.small
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.actionMessage ?: "", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { viewModel.onEvent(AdminDriversUiEvent.ClearActionMessage) }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                contentPadding = MaterialTheme.spacing.small
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.errorMessage ?: "", style = MaterialTheme.typography.bodySmall)
                    JJNOutlinedButton(
                        text = "Retry",
                        onClick = { viewModel.onEvent(AdminDriversUiEvent.Refresh) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Driver List
        if (uiState.filteredDrivers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) "No drivers matching '${uiState.searchQuery}'" else "No drivers registered.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(uiState.filteredDrivers, key = { it.driverId }) { driver ->
                    AdminDriverItemCard(
                        driver = driver,
                        onClick = { viewModel.onEvent(AdminDriversUiEvent.SelectDriver(driver)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }
            }
        }
    }

    // Driver Detail Sheet
    uiState.selectedDriver?.let { selectedDriver ->
        DriverDetailBottomSheet(
            driver = selectedDriver,
            isUpdating = uiState.isUpdating,
            onDismiss = { viewModel.onEvent(AdminDriversUiEvent.SelectDriver(null)) },
            onToggleActive = { isActive ->
                viewModel.onEvent(AdminDriversUiEvent.ToggleDriverActive(selectedDriver.driverId, isActive))
            },
            onToggleApproval = { isApproved ->
                viewModel.onEvent(AdminDriversUiEvent.ToggleDriverApproval(selectedDriver.driverId, isApproved))
            }
        )
    }
}

@Composable
private fun AdminDriverItemCard(
    driver: DriverManagementItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).apply {
        maximumFractionDigits = 0
    }

    JJNOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = MaterialTheme.spacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (driver.user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driver.user.name.take(1).uppercase().ifBlank { "D" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (driver.user.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = driver.user.name.ifBlank { "Driver Partner" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (driver.isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (driver.isOnline) "Online 🟢" else "Offline ⚪",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${driver.vehicleModel} • ${driver.vehiclePlate}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1
                )

                if (driver.user.phone.isNotBlank()) {
                    Text(
                        text = driver.user.phone,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (driver.user.isDriverVerified) "Verified ✅" else "Pending ⚠️",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (driver.user.isDriverVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${driver.completedTrips} trips",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                if (driver.user.ratingCount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⭐ ${String.format(Locale.US, "%.1f", driver.user.ratingAverage)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
