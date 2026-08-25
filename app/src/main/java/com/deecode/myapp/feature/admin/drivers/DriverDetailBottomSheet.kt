package com.deecode.myapp.feature.admin.drivers

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deecode.myapp.domain.model.DriverManagementItem
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailBottomSheet(
    driver: DriverManagementItem,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onToggleApproval: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 0
    }

    val formattedJoinedDate = if ((driver.user.createdAt ?: 0L) > 0L) {
        SimpleDateFormat("dd MMM yyyy", locale).format(Date(driver.user.createdAt!!))
    } else "Recent"

    val formattedStatusDate = if ((driver.user.statusChangedAt ?: 0L) > 0L) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", locale).format(Date(driver.user.statusChangedAt!!))
    } else null

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
            // Driver Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (driver.user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driver.user.name.take(1).uppercase().ifBlank { "D" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (driver.user.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.size(MaterialTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driver.user.name.ifBlank { "Driver Partner" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = driver.user.email,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (driver.isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (driver.isOnline) "Online 🟢" else "Offline ⚪",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (driver.isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (driver.user.isDriverVerified) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (driver.user.isDriverVerified) "Verified ✅" else "Pending ⚠️",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (driver.user.isDriverVerified) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Operational Ride Statistics Grid
            Text(
                text = "📊 Ride & Earnings Statistics",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                JJNOutlinedCard(modifier = Modifier.weight(1f), contentPadding = MaterialTheme.spacing.small) {
                    Text(text = "Completed Trips", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(text = "${driver.completedTrips}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                JJNOutlinedCard(modifier = Modifier.weight(1f), contentPadding = MaterialTheme.spacing.small) {
                    Text(text = "Cancelled Trips", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(text = "${driver.cancelledTrips}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                JJNOutlinedCard(modifier = Modifier.weight(1f), contentPadding = MaterialTheme.spacing.small) {
                    Text(text = "Today's Revenue", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(text = currencyFormat.format(driver.todayEarnings), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                }

                JJNOutlinedCard(modifier = Modifier.weight(1f), contentPadding = MaterialTheme.spacing.small) {
                    Text(text = "Total Revenue", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(text = currencyFormat.format(driver.totalEarnings), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Vehicle & Contact Details Card
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "Vehicle & Account Information",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                DetailRow(label = "Phone Number", value = driver.user.phone.ifBlank { "Not provided" })
                DetailRow(label = "Cab Model", value = driver.vehicleModel)
                DetailRow(label = "License Plate", value = driver.vehiclePlate)
                DetailRow(
                    label = "Partner Rating",
                    value = if (driver.user.ratingCount > 0) "⭐ ${String.format(Locale.US, "%.1f", driver.user.ratingAverage)} (${driver.user.ratingCount} reviews)" else "⭐ 5.0 (No reviews)"
                )
                DetailRow(label = "Partner Since", value = formattedJoinedDate)
                DetailRow(label = "Driver ID", value = "#${driver.driverId.take(12)}...")

                if (formattedStatusDate != null) {
                    DetailRow(
                        label = "Last Modified",
                        value = "$formattedStatusDate by Admin #${driver.user.statusChangedBy?.takeLast(6) ?: "System"}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Admin Actions Card
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "🛡️ Driver Authorization Controls",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Account Active Access Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Driver Application Access",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (driver.user.isActive) "Driver can log in and accept bookings" else "Driver access is suspended",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = driver.user.isActive,
                        onCheckedChange = { onToggleActive(it) },
                        enabled = !isUpdating
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))

                // Verification / Approval Status Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Partner Verification Approval",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (driver.user.isDriverVerified) "Onboarding documents approved ✅" else "Pending partner approval ⚠️",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = driver.user.isDriverVerified,
                        onCheckedChange = { onToggleApproval(it) },
                        enabled = !isUpdating
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            JJNPrimaryButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
