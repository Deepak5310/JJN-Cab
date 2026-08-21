package com.deecode.myapp.feature.driver.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing

@Composable
fun DriverVehicleScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverVehicleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val vehicleTypes = listOf(
        "SEDAN" to "Sedan 🚗",
        "HATCHBACK" to "Hatchback 🚙",
        "SUV" to "SUV 🚙",
        "AUTO" to "Auto Rickshaw 🛺"
    )

    if (uiState.isLoading && uiState.existingVehicle == null) {
        JJNLoadingIndicator(modifier = modifier.fillMaxSize())
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(MaterialTheme.spacing.large)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Vehicle Management 🚗",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Configure your registered vehicle details",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            TextButton(onClick = onNavigateBack) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Success Banner
        if (uiState.isSavedSuccessfully) {
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vehicle details saved successfully! ✅",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { viewModel.onEvent(DriverVehicleUiEvent.ClearSuccess) }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }

        // Error Banner
        if (!uiState.errorMessage.isNullOrBlank()) {
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                contentPadding = MaterialTheme.spacing.medium
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
                            .clickable { viewModel.onEvent(DriverVehicleUiEvent.ClearError) }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }

        // Plate Preview Box
        if (uiState.registrationNumber.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFFDE7))
                    .border(2.dp, Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1565C0))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "IND",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.registrationNumber.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF212121),
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }

        // Vehicle Form Card
        JJNOutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MaterialTheme.spacing.large
        ) {
            // Vehicle Type Selector
            Text(
                text = "Vehicle Category",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                vehicleTypes.take(2).forEach { (typeKey, label) ->
                    FilterChip(
                        selected = uiState.vehicleType == typeKey,
                        onClick = { viewModel.onEvent(DriverVehicleUiEvent.VehicleTypeChanged(typeKey)) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                vehicleTypes.drop(2).forEach { (typeKey, label) ->
                    FilterChip(
                        selected = uiState.vehicleType == typeKey,
                        onClick = { viewModel.onEvent(DriverVehicleUiEvent.VehicleTypeChanged(typeKey)) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Make & Model
            OutlinedTextField(
                value = uiState.makeModel,
                onValueChange = { viewModel.onEvent(DriverVehicleUiEvent.MakeModelChanged(it)) },
                label = { Text("Make & Model *") },
                placeholder = { Text("e.g. Maruti Suzuki Dzire, Hyundai Aura") },
                isError = uiState.makeModelError != null,
                supportingText = uiState.makeModelError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Registration Number
            OutlinedTextField(
                value = uiState.registrationNumber,
                onValueChange = { viewModel.onEvent(DriverVehicleUiEvent.RegistrationNumberChanged(it)) },
                label = { Text("Registration Number *") },
                placeholder = { Text("e.g. DL 01 AB 1234") },
                isError = uiState.registrationError != null,
                supportingText = uiState.registrationError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Color
            OutlinedTextField(
                value = uiState.color,
                onValueChange = { viewModel.onEvent(DriverVehicleUiEvent.ColorChanged(it)) },
                label = { Text("Vehicle Color *") },
                placeholder = { Text("e.g. White, Silver, Midnight Black") },
                isError = uiState.colorError != null,
                supportingText = uiState.colorError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

        // Save Button
        JJNPrimaryButton(
            text = if (uiState.existingVehicle != null) "Update Vehicle Details" else "Save Vehicle Details",
            onClick = { viewModel.onEvent(DriverVehicleUiEvent.SaveVehicle) },
            isLoading = uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        JJNOutlinedButton(
            text = "Back to Profile",
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
