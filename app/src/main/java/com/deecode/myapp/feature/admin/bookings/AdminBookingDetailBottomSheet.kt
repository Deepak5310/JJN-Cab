package com.deecode.myapp.feature.admin.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.feature.customer.bookings.BookingStatusBadge
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingDetailBottomSheet(
    booking: Booking,
    isCancelling: Boolean,
    onDismiss: () -> Unit,
    onCancelBooking: (reason: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("Operational disruption / Fleet safety") }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).apply {
        maximumFractionDigits = 0
    }

    val formattedCreatedDate = if (booking.createdAt > 0L) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(booking.createdAt))
    } else "Unknown"

    val formattedCompletedDate = if ((booking.completedAt ?: 0L) > 0L) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(booking.completedAt!!))
    } else null

    val formattedCancelledDate = if ((booking.cancelledAt ?: 0L) > 0L) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(booking.cancelledAt!!))
    } else null

    val distanceKm = (booking.finalDistanceMeters ?: booking.distanceMeters) / 1000.0
    val durationMin = (booking.finalDurationSeconds ?: booking.estimatedDurationSeconds) / 60
    val fare = booking.finalFare ?: booking.estimatedFare

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(bottom = MaterialTheme.spacing.extraLarge)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Booking #${booking.bookingId.takeLast(8)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = formattedCreatedDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                BookingStatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Route Card
            JJNOutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "Trip Route",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = booking.pickup.address ?: "${booking.pickup.latitude}, ${booking.pickup.longitude}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = booking.destination.address ?: "${booking.destination.latitude}, ${booking.destination.longitude}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Trip Metrics & Fare Card
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "Trip Metrics & Fare",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                DetailRow(label = "Total Fare", value = currencyFormat.format(fare))
                DetailRow(label = "Distance", value = String.format(Locale.US, "%.1f km", distanceKm))
                DetailRow(label = "Duration", value = "$durationMin min")
                DetailRow(label = "Customer ID", value = "#${booking.customerId.take(12)}...")
                DetailRow(label = "Driver ID", value = booking.driverId?.let { "#${it.take(12)}..." } ?: "Unassigned")

                if (formattedCompletedDate != null) {
                    DetailRow(label = "Completed On", value = formattedCompletedDate)
                }
            }

            // Ratings Review Card (if present)
            if (booking.customerRating != null || booking.driverRating != null) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                JJNOutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = MaterialTheme.spacing.medium
                ) {
                    Text(
                        text = "⭐ Trip Ratings & Feedback",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    booking.customerRating?.let { rating ->
                        DetailRow(label = "Customer Rating", value = "${"★".repeat(rating)}${"☆".repeat(5 - rating)}")
                        if (!booking.customerReview.isNullOrBlank()) {
                            Text(
                                text = "Note: \"${booking.customerReview}\"",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    booking.driverRating?.let { rating ->
                        Spacer(modifier = Modifier.height(4.dp))
                        DetailRow(label = "Driver Rating", value = "${"★".repeat(rating)}${"☆".repeat(5 - rating)}")
                        if (!booking.driverReview.isNullOrBlank()) {
                            Text(
                                text = "Note: \"${booking.driverReview}\"",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // Cancellation Card (if cancelled)
            if (booking.status in setOf(BookingStatus.CANCELLED, BookingStatus.CANCELLED_BY_CUSTOMER, BookingStatus.CANCELLED_BY_DRIVER)) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                JJNCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentPadding = MaterialTheme.spacing.medium
                ) {
                    Text(
                        text = "Cancellation Record",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reason: ${booking.cancellationReason ?: "No reason provided"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (formattedCancelledDate != null) {
                        Text(
                            text = "Cancelled at: $formattedCancelledDate",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (!booking.cancelledBy.isNullOrBlank()) {
                        Text(
                            text = "Cancelled by: #${booking.cancelledBy.takeLast(8)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Admin Cancel Action (Active rides only)
            if (booking.status.isActive) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                JJNOutlinedButton(
                    text = "Cancel Ride as Admin 🛑",
                    onClick = { showCancelConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            JJNPrimaryButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Cancellation Confirmation Dialog
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Confirm Admin Cancellation") },
            text = {
                Column {
                    Text("Are you sure you want to cancel booking #${booking.bookingId.takeLast(8)}? Both the customer and driver will be notified immediately.")
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Cancellation Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                        onCancelBooking(cancelReason)
                    },
                    enabled = !isCancelling
                ) {
                    Text("Confirm Cancellation", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
