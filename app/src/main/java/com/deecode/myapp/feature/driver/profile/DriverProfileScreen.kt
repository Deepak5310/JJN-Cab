package com.deecode.myapp.feature.driver.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing
import java.util.Locale

@Composable
fun DriverProfileScreen(
    onManageVehicle: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    if (uiState.isLoading && uiState.user == null) {
        JJNLoadingIndicator(modifier = modifier.fillMaxSize())
        return
    }

    val user = uiState.user
    val vehicle = uiState.vehicle

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar Header
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.initials,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Text(
            text = user?.name?.ifBlank { "Driver Partner" } ?: "Driver Partner",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Text(
            text = user?.email ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Status Badges Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(if (user?.isDriverVerified == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (user?.isDriverVerified == true) "Verified Partner ✅" else "Pending Approval ⚠️",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (user?.isDriverVerified == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if ((user?.ratingCount ?: 0) > 0) "⭐ ${String.format(Locale.US, "%.1f", user?.ratingAverage)} (${user?.ratingCount})" else "⭐ 5.0 (New)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Quick Stats Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            JJNOutlinedCard(
                modifier = Modifier.weight(1f),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "Completed Rides",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "${uiState.completedRides} trips",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            JJNOutlinedCard(
                modifier = Modifier.weight(1f),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "Partner Since",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = uiState.formattedJoinedDate,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Success Banner
        if (!uiState.successMessage.isNullOrBlank()) {
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
                    Text(text = uiState.successMessage ?: "", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { viewModel.onEvent(DriverProfileUiEvent.ClearSuccessMessage) }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        }

        // Error Banner
        if (!uiState.errorMessage.isNullOrBlank()) {
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
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { viewModel.onEvent(DriverProfileUiEvent.ClearError) }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        }

        // Assigned Vehicle Card
        JJNCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MaterialTheme.spacing.medium
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assigned Vehicle 🚗",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (vehicle != null) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = vehicle.vehicleType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            if (vehicle != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Make & Model", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(text = vehicle.makeModel, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Plate Number", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(text = vehicle.formattedPlate, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }

                if (vehicle.color.isNotBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text(text = "Color", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(text = vehicle.color, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                }
            } else {
                Text(
                    text = "No vehicle details registered yet.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            JJNOutlinedButton(
                text = if (vehicle != null) "Manage Vehicle Details ✏️" else "Add Vehicle Details ➕",
                onClick = onManageVehicle,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Personal Information / Edit Card
        if (uiState.isEditing) {
            // Edit Mode Form
            JJNOutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.large
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                OutlinedTextField(
                    value = uiState.editName,
                    onValueChange = { viewModel.onEvent(DriverProfileUiEvent.NameChanged(it)) },
                    label = { Text("Full Name *") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                OutlinedTextField(
                    value = uiState.editPhone,
                    onValueChange = { viewModel.onEvent(DriverProfileUiEvent.PhoneChanged(it)) },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    isError = uiState.phoneError != null,
                    supportingText = uiState.phoneError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                // Immutable Email Info Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔒", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Email (${user?.email ?: ""}) is linked to your Google partner credentials and cannot be modified.",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    JJNOutlinedButton(
                        text = "Cancel",
                        onClick = { viewModel.onEvent(DriverProfileUiEvent.CancelEditing) },
                        modifier = Modifier.weight(1f)
                    )

                    JJNPrimaryButton(
                        text = "Save Changes",
                        onClick = { viewModel.onEvent(DriverProfileUiEvent.SaveProfile) },
                        isLoading = uiState.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // View Mode Card
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.large
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Partner Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    JJNOutlinedButton(
                        text = "Edit ✏️",
                        onClick = { viewModel.onEvent(DriverProfileUiEvent.StartEditing) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

                ProfileDetailItem(label = "Full Name", value = user?.name?.ifBlank { "Not provided" } ?: "Not provided")
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                ProfileDetailItem(label = "Email Address", value = user?.email ?: "Not provided")
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                ProfileDetailItem(label = "Phone Number", value = user?.phone?.ifBlank { "Not provided" } ?: "Not provided")
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                ProfileDetailItem(label = "Account Access", value = if (user?.isActive == true) "Active 🟢" else "Suspended 🔴")
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                ProfileDetailItem(label = "Partner ID", value = "#${user?.uid?.take(10) ?: ""}...")
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Settings Button
        JJNCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToSettings),
            contentPadding = MaterialTheme.spacing.medium
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚙️", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                    Text(
                        text = "App Settings (Theme, Alerts & Info)",
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

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Logout Button
        JJNOutlinedButton(
            text = "Logout 🚪",
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}

@Composable
private fun ProfileDetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
