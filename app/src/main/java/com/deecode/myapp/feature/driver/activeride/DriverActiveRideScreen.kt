package com.deecode.myapp.feature.driver.activeride

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DriverActiveRideScreen(
    activeBooking: Booking?,
    customerName: String?,
    isUpdatingStatus: Boolean,
    rideStatusError: String?,
    onUpdateStatus: (BookingStatus) -> Unit,
    onCompleteRide: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).apply {
        maximumFractionDigits = 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (activeBooking == null) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏎️", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = "No Active Ride",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "When you accept a customer booking, live navigation and passenger details will appear here.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "📍 Live Turn-by-Turn Navigation & OTP",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Active trip management includes pickup point routing, ride start OTP confirmation, and in-app customer contact.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            // Live Accepted Booking
            Spacer(modifier = Modifier.height(16.dp))

            // Error Banner
            if (!rideStatusError.isNullOrBlank()) {
                JJNCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.medium),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = rideStatusError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (activeBooking.status) {
                        BookingStatus.ACCEPTED -> "🚘"
                        BookingStatus.DRIVER_ARRIVING -> "📍"
                        BookingStatus.IN_PROGRESS -> "🏎️"
                        else -> "🏁"
                    },
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = when (activeBooking.status) {
                    BookingStatus.ACCEPTED -> "Assigned • Head to Pickup"
                    BookingStatus.DRIVER_ARRIVING -> "Arrived at Pickup Point"
                    BookingStatus.IN_PROGRESS -> "Trip in Progress"
                    else -> "Ride Status: ${activeBooking.status.name}"
                },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = when (activeBooking.status) {
                    BookingStatus.ACCEPTED -> "Passenger is waiting for you at the pickup location."
                    BookingStatus.DRIVER_ARRIVING -> "You have arrived. Verify rider and start the trip."
                    BookingStatus.IN_PROGRESS -> "Navigating towards passenger's destination."
                    else -> "Status: ${activeBooking.status.name}"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Customer Info Card
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (customerName ?: "P").take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                    Column {
                        Text(
                            text = "Rider: ${customerName ?: "Passenger"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Booking Ref: #${activeBooking.bookingId.takeLast(8)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Trip Summary Card
            JJNOutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "Trip Details",
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
                            text = "Pickup Point",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = activeBooking.pickup.address ?: "${activeBooking.pickup.latitude}, ${activeBooking.pickup.longitude}",
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
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = activeBooking.destination.address ?: "${activeBooking.destination.latitude}, ${activeBooking.destination.longitude}",
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
                            text = String.format(Locale.US, "%.1f km", activeBooking.distanceMeters / 1000.0),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column {
                        Text(
                            text = "Est. Time",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "${activeBooking.estimatedDurationSeconds / 60} min",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Estimated Fare",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = currencyFormat.format(activeBooking.estimatedFare),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Action Transition CTA
            when (activeBooking.status) {
                BookingStatus.ACCEPTED -> {
                    JJNPrimaryButton(
                        text = if (isUpdatingStatus) "Updating..." else "Arrived at Pickup",
                        onClick = { onUpdateStatus(BookingStatus.DRIVER_ARRIVING) },
                        enabled = !isUpdatingStatus,
                        isLoading = isUpdatingStatus,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BookingStatus.DRIVER_ARRIVING -> {
                    JJNPrimaryButton(
                        text = if (isUpdatingStatus) "Starting..." else "Start Ride",
                        onClick = { onUpdateStatus(BookingStatus.IN_PROGRESS) },
                        enabled = !isUpdatingStatus,
                        isLoading = isUpdatingStatus,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BookingStatus.IN_PROGRESS -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                    ) {
                        JJNCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentPadding = MaterialTheme.spacing.medium
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "🏎️", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                Text(
                                    text = "Trip in Progress • En route to destination",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        JJNPrimaryButton(
                            text = if (isUpdatingStatus) "Completing Ride..." else "🏁 Complete Ride",
                            onClick = onCompleteRide,
                            enabled = !isUpdatingStatus,
                            isLoading = isUpdatingStatus,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}
