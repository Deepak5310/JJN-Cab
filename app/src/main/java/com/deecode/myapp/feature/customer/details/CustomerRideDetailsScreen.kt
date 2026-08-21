package com.deecode.myapp.feature.customer.details

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.feature.customer.bookings.BookingStatusBadge
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.components.map.JJNMap
import com.deecode.myapp.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRideDetailsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerRideDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ride Details",
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
                        onClick = { viewModel.onEvent(CustomerRideDetailsUiEvent.Retry) }
                    )
                }
            }

            val booking = uiState.booking
            if (booking == null) {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = "No ride details found.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                JJNPrimaryButton(
                    text = "Go Back",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Header: Status Badge + Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ride #${booking.bookingId.takeLast(8)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    BookingStatusBadge(status = booking.status)
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Map View
                JJNOutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentPadding = 0.dp
                ) {
                    JJNMap(
                        modifier = Modifier.fillMaxSize(),
                        pickupLocation = booking.pickup,
                        destinationLocation = booking.destination
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Route & Locations
                JJNOutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = MaterialTheme.spacing.medium
                ) {
                    Text(
                        text = "Route Information",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Pickup
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Column {
                            Text(
                                text = "Pickup Location",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = booking.pickup.address ?: "${booking.pickup.latitude}, ${booking.pickup.longitude}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (uiState.formattedCreatedAt.isNotBlank()) {
                                Text(
                                    text = "Requested: ${uiState.formattedCreatedAt}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Destination
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Column {
                            Text(
                                text = "Destination",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = booking.destination.address ?: "${booking.destination.latitude}, ${booking.destination.longitude}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (booking.status == BookingStatus.COMPLETED && uiState.formattedCompletedAt.isNotBlank()) {
                                Text(
                                    text = "Completed: ${uiState.formattedCompletedAt}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF2E7D32))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Trip Metrics & Payment
                JJNOutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = MaterialTheme.spacing.medium
                ) {
                    Text(
                        text = "Trip Summary & Fare",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

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
                                text = "Duration",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = uiState.formattedDuration,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (booking.status == BookingStatus.COMPLETED) "Total Paid" else "Estimated Fare",
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

                // Driver Card (if driver assigned)
                if (!booking.driverId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    JJNCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentPadding = MaterialTheme.spacing.medium
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (uiState.driverName ?: "D").take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                            Column {
                                Text(
                                    text = "Driver: ${uiState.driverName ?: "Assigned Driver"}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Driver ID: #${booking.driverId.takeLast(8)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }

                // Rating Review Card (if rated)
                if (booking.customerRating != null) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    JJNOutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = MaterialTheme.spacing.medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Rating",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "${"★".repeat(booking.customerRating)}${"☆".repeat(5 - booking.customerRating)}",
                                color = Color(0xFFFFB300),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (!booking.customerReview.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${booking.customerReview}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                // Cancellation Details (if cancelled)
                if (booking.status in setOf(BookingStatus.CANCELLED, BookingStatus.CANCELLED_BY_CUSTOMER, BookingStatus.CANCELLED_BY_DRIVER)) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    JJNCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        contentPadding = MaterialTheme.spacing.medium
                    ) {
                        Text(
                            text = "Cancellation Details",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reason: ${booking.cancellationReason ?: "No reason specified"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (uiState.formattedCancelledAt.isNotBlank()) {
                            Text(
                                text = "Cancelled on: ${uiState.formattedCancelledAt}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                JJNPrimaryButton(
                    text = "Back to Bookings",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
