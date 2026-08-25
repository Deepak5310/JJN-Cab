package com.deecode.myapp.feature.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.domain.model.ThemeMode
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(MaterialTheme.spacing.large)
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Section 1: Appearance / Theme
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            ThemeSelectionCard(
                title = "System",
                icon = "🌓",
                isSelected = uiState.themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.onEvent(SettingsUiEvent.SetThemeMode(ThemeMode.SYSTEM)) },
                modifier = Modifier.weight(1f)
            )

            ThemeSelectionCard(
                title = "Light",
                icon = "☀️",
                isSelected = uiState.themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.onEvent(SettingsUiEvent.SetThemeMode(ThemeMode.LIGHT)) },
                modifier = Modifier.weight(1f)
            )

            ThemeSelectionCard(
                title = "Dark",
                icon = "🌙",
                isSelected = uiState.themeMode == ThemeMode.DARK,
                onClick = { viewModel.onEvent(SettingsUiEvent.SetThemeMode(ThemeMode.DARK)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Section 2: Notifications
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        JJNCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MaterialTheme.spacing.medium
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ride & Status Alerts",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Receive notifications for booking status, driver arrivals, and OTP alerts",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsUiEvent.SetNotificationsEnabled(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Section 3: Legal & Information
        Text(
            text = "About & Legal",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        JJNCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MaterialTheme.spacing.medium
        ) {
            SettingsNavigationRow(
                title = "About JJN Cab",
                icon = "🚕",
                onClick = { viewModel.onEvent(SettingsUiEvent.OpenAboutDialog) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))

            SettingsNavigationRow(
                title = "Privacy Policy",
                icon = "🛡️",
                onClick = { viewModel.onEvent(SettingsUiEvent.OpenPrivacySheet) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))

            SettingsNavigationRow(
                title = "Terms & Conditions",
                icon = "📜",
                onClick = { viewModel.onEvent(SettingsUiEvent.OpenTermsSheet) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📱", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                    Text(
                        text = "App Version",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Text(
                    text = "v${uiState.appVersion} (${uiState.appBuild})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

        // Section 4: Account / Logout
        JJNOutlinedButton(
            text = "Log Out 🚪",
            onClick = { viewModel.signOut(onLogout) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }

    // About Dialog
    if (uiState.isAboutDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingsUiEvent.CloseAboutDialog) },
            title = {
                Text(
                    text = "About JJN Cab 🚕",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "JJN Cab is a modern, reliable, and secure taxi booking platform designed to connect riders and driver partners seamlessly.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Text(
                        text = "Features:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "• Real-time trip pricing and routing\n• Live map tracking and OTP verification\n• Partner vehicle fleet management\n• Role-based security & audit logging",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text(
                        text = "Version ${uiState.appVersion} • Built with Jetpack Compose",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SettingsUiEvent.CloseAboutDialog) }) {
                    Text("Close")
                }
            }
        )
    }

    // Privacy Policy Bottom Sheet
    if (uiState.isPrivacySheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(SettingsUiEvent.ClosePrivacySheet) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.large)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Privacy Policy 🛡️",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                Text(
                    text = "1. Information We Collect\nWe collect your account info (name, email, phone) and real-time GPS location during active rides to facilitate dispatch and navigation.\n\n" +
                            "2. Data Security\nAll customer and driver data is strictly secured via Firebase Authentication and server-authoritative Firestore Security Rules.\n\n" +
                            "3. Location Privacy\nDriver location is shared exclusively with active riders during in-progress trips.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }

    // Terms & Conditions Bottom Sheet
    if (uiState.isTermsSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(SettingsUiEvent.CloseTermsSheet) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.large)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Terms & Conditions 📜",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                Text(
                    text = "1. User Responsibilities\nUsers agree to provide accurate registration details and maintain vehicle roadworthiness.\n\n" +
                            "2. Fare Calculation\nTrip fares are computed using standard base pricing, distance, duration, and applicable tier multipliers.\n\n" +
                            "3. Cancellation Policy\nRides may be cancelled before pickup. Excessive cancellations may impact account rating.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }
}

@Composable
private fun ThemeSelectionCard(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    icon: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }

        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
