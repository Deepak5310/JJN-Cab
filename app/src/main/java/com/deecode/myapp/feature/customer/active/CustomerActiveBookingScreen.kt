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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.deecode.myapp.ui.components.dialog.CancellationReasonBottomSheet
import com.deecode.myapp.ui.components.map.JJNMap
import com.deecode.myapp.ui.theme.spacing

private val CustomerCancellationReasons = listOf(
    "Change of plans / No longer needed",
    "Driver taking too long to arrive",
    "Wrong pickup or destination location",
    "Found alternative transport",
    "Other reason"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerActiveBookingScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerActiveBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showCancelDialog by remember { mutableStateOf(false) }

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

            // Cancellation Error
            if (!uiState.cancellationError.isNullOrBlank()) {
                JJNCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.medium),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = uiState.cancellationError ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    JJNOutlinedButton(
                        text = "Dismiss",
                        onClick = { viewModel.onEvent(CustomerActiveBookingUiEvent.ClearCancellationError) }
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
                val isCancelled = booking.status in setOf(
                    BookingStatus.CANCELLED,
                    BookingStatus.CANCELLED_BY_CUSTOMER,
                    BookingStatus.CANCELLED_BY_DRIVER
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCancelled) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (booking.status) {
                            BookingStatus.REQUESTED, BookingStatus.SEARCHING_DRIVER -> "🚕"
                            BookingStatus.ACCEPTED, BookingStatus.DRIVER_ARRIVING -> "🚘"
                            BookingStatus.IN_PROGRESS -> "🏎️"
                            BookingStatus.COMPLETED -> "🏁"
                            BookingStatus.CANCELLED, BookingStatus.CANCELLED_BY_CUSTOMER, BookingStatus.CANCELLED_BY_DRIVER -> "❌"
                            else -> "📍"
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
                        BookingStatus.COMPLETED -> "Ride Completed 🏁"
                        BookingStatus.CANCELLED_BY_CUSTOMER -> "Ride Cancelled"
                        BookingStatus.CANCELLED_BY_DRIVER -> "Cancelled by Driver"
                        BookingStatus.CANCELLED -> "Ride Cancelled"
                        else -> "Ride Status: ${booking.status.name}"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = when (booking.status) {
                        BookingStatus.REQUESTED -> "Nearby drivers are receiving your request..."
                        BookingStatus.SEARCHING_DRIVER -> "Finding the closest available cab for you..."
                        BookingStatus.ACCEPTED -> "A driver has accepted your ride request."
                        BookingStatus.DRIVER_ARRIVING -> "Driver is heading towards your pickup point."
                        BookingStatus.IN_PROGRESS -> "You are on your way to destination."
                        BookingStatus.COMPLETED -> "You have reached your destination. Thank you for riding with JJN Cab!"
                        BookingStatus.CANCELLED_BY_CUSTOMER -> "You cancelled this booking: ${booking.cancellationReason ?: "Change of plans"}"
                        BookingStatus.CANCELLED_BY_DRIVER -> "Driver had to cancel: ${booking.cancellationReason ?: "Unavailable"}"
                        BookingStatus.CANCELLED -> "This ride has been cancelled."
                        else -> "Current status: ${booking.status.name}"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // 2. Live Map Card
                if (!isCancelled) {
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
                }

                // 3. Real-time Status Card
                JJNCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (isCancelled) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isCancelled) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    contentPadding = MaterialTheme.spacing.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (booking.status.isActive) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(text = if (isCancelled) "❌" else "🏁", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                        Column {
                            Text(
                                text = "Status: ${booking.status.name}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (uiState.driverLocation != null && booking.status.isActive) {
                                Text(
                                    text = "🟢 Live driver location active",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                    )
                                )
                            } else if (booking.cancellationReason != null) {
                                Text(
                                    text = "Reason: ${booking.cancellationReason}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Assigned Driver Vehicle Details
                uiState.driverVehicle?.let { vehicle ->
                    JJNCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentPadding = MaterialTheme.spacing.medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Assigned Cab 🚖",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${vehicle.makeModel} (${vehicle.color})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = vehicle.formattedPlate,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                }

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
                                text = if (booking.status == BookingStatus.COMPLETED) "Final Fare" else "Estimated Fare",
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

                // Actions: Cancel Ride when Active, or Rate / Back to Home when completed/cancelled
                if (booking.status.isActive) {
                    JJNOutlinedButton(
                        text = "Cancel Ride",
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (booking.status == BookingStatus.COMPLETED) {
                    if (uiState.isRatingSubmitted || booking.customerRating != null) {
                        JJNCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentPadding = MaterialTheme.spacing.medium
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Rating: ${"★".repeat(booking.customerRating ?: 5)}${"☆".repeat(5 - (booking.customerRating ?: 5))}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Submitted ✓",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    } else {
                        JJNOutlinedButton(
                            text = "Rate your Driver 🌟",
                            onClick = { viewModel.onEvent(CustomerActiveBookingUiEvent.OpenRatingSheet) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    }

                    JJNPrimaryButton(
                        text = "Back to Home",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    JJNPrimaryButton(
                        text = "Back to Home",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (uiState.isRatingSheetVisible) {
                    com.deecode.myapp.ui.components.dialog.RatingBottomSheet(
                        title = "Rate your Driver",
                        targetName = "your driver",
                        isSubmitting = uiState.isSubmittingRating,
                        errorMessage = uiState.ratingError,
                        onDismiss = { viewModel.onEvent(CustomerActiveBookingUiEvent.CloseRatingSheet) },
                        onSubmit = { rating, review ->
                            viewModel.onEvent(CustomerActiveBookingUiEvent.SubmitRating(rating, review))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Booking Reference Pill
                Text(
                    text = "Booking Ref: #${booking.bookingId.takeLast(10)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (showCancelDialog) {
                    CancellationReasonBottomSheet(
                        title = "Cancel Ride",
                        reasons = CustomerCancellationReasons,
                        isLoading = uiState.isCancelling,
                        onConfirm = { reason ->
                            viewModel.onEvent(CustomerActiveBookingUiEvent.CancelBooking(booking.bookingId, reason))
                            showCancelDialog = false
                        },
                        onDismiss = { showCancelDialog = false }
                    )
                }
            }
        }
    }
}
