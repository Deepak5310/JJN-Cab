package com.deecode.myapp.feature.driver.requests

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverRequestsScreen(
    isOnline: Boolean,
    pendingBookings: List<Booking>,
    dismissedBookingIds: Set<String>,
    acceptingBookingId: String?,
    actionMessage: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onAcceptBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    onClearActionMessage: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    val visibleBookings = pendingBookings.filter { it.bookingId !in dismissedBookingIds }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium
            )
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ride Requests 📡",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isOnline) "Real-time dispatch stream" else "Offline mode",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (isOnline) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${visibleBookings.size} Available",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Action / Notice Message Banner
        if (!actionMessage.isNullOrBlank()) {
            JJNCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionMessage,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(4.dp)
                    )
                }
            }
        }

        if (!isOnline) {
            // Offline Screen
            Spacer(modifier = Modifier.height(40.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚪", style = MaterialTheme.typography.headlineLarge)
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Text(
                    text = "You are currently Offline",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Text(
                    text = "Switch to Online on the Dashboard to start receiving incoming passenger ride requests.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            return
        }

        // Error Banner
        if (!errorMessage.isNullOrBlank()) {
            JJNCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                JJNOutlinedButton(
                    text = "Retry",
                    onClick = onRefresh
                )
            }
        }

        if (isLoading && visibleBookings.isEmpty()) {
            // Loading State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (visibleBookings.isEmpty()) {
            // Empty State
            Spacer(modifier = Modifier.height(60.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🚕", style = MaterialTheme.typography.headlineLarge)
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Text(
                    text = "Scanning for Rides...",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Text(
                    text = "No open ride requests right now. New passenger requests will appear here instantly.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            // Requests List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                items(
                    items = visibleBookings,
                    key = { it.bookingId }
                ) { booking ->
                    BookingRequestCard(
                        booking = booking,
                        currencyFormat = currencyFormat,
                        isAccepting = acceptingBookingId == booking.bookingId,
                        isAnyActionInProgress = acceptingBookingId != null,
                        onAccept = { onAcceptBooking(booking.bookingId) },
                        onReject = { onRejectBooking(booking.bookingId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingRequestCard(
    booking: Booking,
    currencyFormat: NumberFormat,
    isAccepting: Boolean,
    isAnyActionInProgress: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val requestedTime = if (booking.createdAt != null && booking.createdAt > 0L) {
        timeFormat.format(Date(booking.createdAt))
    } else {
        "Just now"
    }

    JJNOutlinedCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = MaterialTheme.spacing.medium
    ) {
        // Header (Fare & Time)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "REQUESTED • $requestedTime",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Text(
                text = currencyFormat.format(booking.estimatedFare),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

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
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = booking.destination.address ?: "${booking.destination.latitude}, ${booking.destination.longitude}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Metrics (Distance & Duration)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛣️ ${String.format(Locale.US, "%.1f km", booking.distanceMeters / 1000.0)}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )

            Text(
                text = "⏱️ ${booking.estimatedDurationSeconds / 60} mins",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )

            Text(
                text = "Ref: #${booking.bookingId.takeLast(6)}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            JJNOutlinedButton(
                text = "Reject",
                onClick = onReject,
                enabled = !isAnyActionInProgress,
                modifier = Modifier.weight(1f)
            )

            JJNPrimaryButton(
                text = if (isAccepting) "Accepting..." else "Accept Ride",
                onClick = onAccept,
                enabled = !isAnyActionInProgress,
                isLoading = isAccepting,
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}
