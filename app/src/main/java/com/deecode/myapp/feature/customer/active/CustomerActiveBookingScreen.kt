package com.deecode.myapp.feature.customer.active

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.components.map.JJNMap
import com.deecode.myapp.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerActiveBookingScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerActiveBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Active Ride",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.isLoading && uiState.booking == null) {
            JJNLoadingIndicator(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error State
            if (!uiState.errorMessage.isNullOrBlank()) {
                JJNCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.medium),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    JJNOutlinedButton(
                        text = "Retry",
                        onClick = { viewModel.onEvent(CustomerActiveBookingUiEvent.Retry) }
                    )
                }
            }

            val booking = uiState.booking
            if (booking == null) {
                // Empty State
                Spacer(modifier = Modifier.height(60.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🚕", style = MaterialTheme.typography.headlineLarge)
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Text(
                    text = "No Active Ride",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "You don't have any ongoing or requested rides at the moment.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                JJNPrimaryButton(
                    text = "Book a Ride",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Active Booking Details
                // 1. Live Status Header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (booking.status) {
                            BookingStatus.REQUESTED, BookingStatus.SEARCHING_DRIVER -> "🚕"
                            BookingStatus.ACCEPTED, BookingStatus.DRIVER_ARRIVING -> "🚘"
                            BookingStatus.IN_PROGRESS -> "🏎️"
                            else -> "🏁"
                        },
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Text(
                    text = when (booking.status) {
                        BookingStatus.REQUESTED -> "Ride Requested"
                        BookingStatus.SEARCHING_DRIVER -> "Searching for Driver"
                        BookingStatus.ACCEPTED -> "Driver Confirmed"
                        BookingStatus.DRIVER_ARRIVING -> "Driver Arriving"
                        BookingStatus.IN_PROGRESS -> "Trip in Progress"
                        else -> "Ride Status: ${booking.status.name}"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = when (booking.status) {
                        BookingStatus.REQUESTED -> "Nearby drivers are receiving your request..."
                        BookingStatus.SEARCHING_DRIVER -> "Finding the closest available cab for you..."
                        BookingStatus.ACCEPTED -> "A driver has accepted your ride request."
                        BookingStatus.DRIVER_ARRIVING -> "Driver is heading towards your pickup point."
                        BookingStatus.IN_PROGRESS -> "You are on your way to destination."
                        else -> "Current status: ${booking.status.name}"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // 2. Live Map Card with Pickup, Destination, and Driver Live Location
                JJNOutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentPadding = 0.dp
                ) {
                    JJNMap(
                        modifier = Modifier.fillMaxSize(),
                        pickupLocation = booking.pickup,
                        destinationLocation = booking.destination,
                        driverLocation = uiState.driverLocation,
                        hasLocationPermission = uiState.hasLocationPermission
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // 3. Real-time Status Card
                JJNCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentPadding = MaterialTheme.spacing.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                        Column {
                            Text(
                                text = "Status: ${booking.status.name}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (uiState.driverLocation != null) {
                                Text(
                                    text = "🟢 Live driver location active",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                    )
                                )
                            } else if (uiState.formattedCreatedAt.isNotBlank()) {
                                Text(
                                    text = "Requested at: ${uiState.formattedCreatedAt}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // 4. Trip Details Card
                JJNOutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = MaterialTheme.spacing.medium
                ) {
                    Text(
                        text = "Trip Summary",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Pickup
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Column {
                            Text(
                                text = "Pickup",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = booking.pickup.address ?: "${booking.pickup.latitude}, ${booking.pickup.longitude}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Destination
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Column {
                            Text(
                                text = "Destination",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = booking.destination.address ?: "${booking.destination.latitude}, ${booking.destination.longitude}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Distance",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = uiState.formattedDistance,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column {
                            Text(
                                text = "Est. Duration",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = uiState.formattedDuration,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Estimated Fare",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = uiState.formattedFare,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Booking Reference Pill
                Text(
                    text = "Booking Ref: #${booking.bookingId.takeLast(10)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                JJNPrimaryButton(
                    text = "Back to Home",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
