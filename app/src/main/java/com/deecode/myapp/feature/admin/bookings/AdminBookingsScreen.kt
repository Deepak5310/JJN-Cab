package com.deecode.myapp.feature.admin.bookings

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
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
import com.deecode.myapp.domain.model.Booking
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.feature.customer.bookings.BookingStatusBadge
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminBookingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading && uiState.allBookings.isEmpty()) {
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

    val activeCount = uiState.allBookings.count { it.status.isActive }
    val completedCount = uiState.allBookings.count { it.status == BookingStatus.COMPLETED }
    val cancelledCount = uiState.allBookings.count {
        it.status in setOf(BookingStatus.CANCELLED, BookingStatus.CANCELLED_BY_CUSTOMER, BookingStatus.CANCELLED_BY_DRIVER)
    }

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
                    text = "Fleet Bookings 📋",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${uiState.allBookings.size} total rides recorded",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onEvent(AdminBookingsUiEvent.SearchQueryChanged(it)) },
            label = { Text("Search by ID, customer, driver, address...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { viewModel.onEvent(AdminBookingsUiEvent.SearchQueryChanged("")) }
                            .padding(8.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Status Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            item {
                FilterChip(
                    selected = uiState.statusFilter == BookingStatusFilter.ALL,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectStatusFilter(BookingStatusFilter.ALL)) },
                    label = { Text("All (${uiState.allBookings.size})") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.statusFilter == BookingStatusFilter.ACTIVE,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectStatusFilter(BookingStatusFilter.ACTIVE)) },
                    label = { Text("Active 🏎️ ($activeCount)") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.statusFilter == BookingStatusFilter.COMPLETED,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectStatusFilter(BookingStatusFilter.COMPLETED)) },
                    label = { Text("Completed 🏁 ($completedCount)") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.statusFilter == BookingStatusFilter.CANCELLED,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectStatusFilter(BookingStatusFilter.CANCELLED)) },
                    label = { Text("Cancelled ❌ ($cancelledCount)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Date Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            item {
                FilterChip(
                    selected = uiState.dateFilter == BookingDateFilter.ALL,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectDateFilter(BookingDateFilter.ALL)) },
                    label = { Text("All Time") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.dateFilter == BookingDateFilter.TODAY,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectDateFilter(BookingDateFilter.TODAY)) },
                    label = { Text("Today") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.dateFilter == BookingDateFilter.THIS_WEEK,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectDateFilter(BookingDateFilter.THIS_WEEK)) },
                    label = { Text("This Week") }
                )
            }
            item {
                FilterChip(
                    selected = uiState.dateFilter == BookingDateFilter.THIS_MONTH,
                    onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectDateFilter(BookingDateFilter.THIS_MONTH)) },
                    label = { Text("This Month") }
                )
            }
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
                            .clickable { viewModel.onEvent(AdminBookingsUiEvent.ClearActionMessage) }
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
                        onClick = { viewModel.onEvent(AdminBookingsUiEvent.Refresh) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Bookings List
        if (uiState.filteredBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) "No bookings matching '${uiState.searchQuery}'" else "No bookings found matching selected filters.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(uiState.filteredBookings, key = { it.bookingId }) { booking ->
                    AdminBookingCard(
                        booking = booking,
                        onClick = { viewModel.onEvent(AdminBookingsUiEvent.SelectBooking(booking)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }
            }
        }
    }

    // Detail Bottom Sheet
    uiState.selectedBooking?.let { selectedBooking ->
        AdminBookingDetailBottomSheet(
            booking = selectedBooking,
            isCancelling = uiState.isCancelling,
            onDismiss = { viewModel.onEvent(AdminBookingsUiEvent.SelectBooking(null)) },
            onCancelBooking = { reason ->
                viewModel.onEvent(AdminBookingsUiEvent.CancelBookingAsAdmin(selectedBooking.bookingId, reason))
            }
        )
    }
}

@Composable
private fun AdminBookingCard(
    booking: Booking,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).apply {
        maximumFractionDigits = 0
    }

    val formattedDate = if (booking.createdAt > 0L) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(booking.createdAt))
    } else ""

    val fare = booking.finalFare ?: booking.estimatedFare

    JJNOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = MaterialTheme.spacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ref: #${booking.bookingId.takeLast(6)} • $formattedDate",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            BookingStatusBadge(status = booking.status)
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = booking.pickup.address ?: "${booking.pickup.latitude}, ${booking.pickup.longitude}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = booking.destination.address ?: "${booking.destination.latitude}, ${booking.destination.longitude}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Driver: ${booking.driverId?.let { "#${it.takeLast(6)}" } ?: "Unassigned"}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = currencyFormat.format(fare),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
