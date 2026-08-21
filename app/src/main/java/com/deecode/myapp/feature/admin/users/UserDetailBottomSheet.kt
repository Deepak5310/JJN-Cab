package com.deecode.myapp.feature.admin.users

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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailBottomSheet(
    user: User,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onToggleDriverVerification: (Boolean) -> Unit,
    onChangeRole: (UserRole) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val formattedJoinedDate = if ((user.createdAt ?: 0L) > 0L) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(user.createdAt!!))
    } else "Unknown"

    val formattedStatusDate = if ((user.statusChangedAt ?: 0L) > 0L) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(user.statusChangedAt!!))
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
            // Header with Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase().ifBlank { "U" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (user.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.size(MaterialTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.ifBlank { "Unnamed User" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(if (user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (user.isActive) "Active 🟢" else "Disabled 🔴",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (user.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Contact & Profile Details Card
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "Profile & Account Information",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                DetailRow(label = "Phone Number", value = user.phone.ifBlank { "Not provided" })
                DetailRow(label = "Role", value = user.role.name)
                DetailRow(
                    label = "User Rating",
                    value = if (user.ratingCount > 0) "⭐ ${String.format(Locale.US, "%.1f", user.ratingAverage)} (${user.ratingCount} reviews)" else "⭐ 5.0 (No reviews)"
                )
                DetailRow(label = "Member Since", value = formattedJoinedDate)
                DetailRow(label = "User ID", value = "#${user.uid.take(12)}...")

                if (formattedStatusDate != null) {
                    DetailRow(
                        label = "Last Modified",
                        value = "$formattedStatusDate by Admin #${user.statusChangedBy?.takeLast(6) ?: "System"}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Account Status & Permissions Control Card
            JJNCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentPadding = MaterialTheme.spacing.medium
            ) {
                Text(
                    text = "🛡️ Administration Controls",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Toggle Active Access
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Application Access",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (user.isActive) "User can log in and book rides" else "User access is disabled/suspended",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = user.isActive,
                        onCheckedChange = { onToggleActive(it) },
                        enabled = !isUpdating
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))

                // Driver Verification Toggle
                if (user.role == UserRole.DRIVER) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Driver Verification",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (user.isDriverVerified) "Driver partner documents approved ✅" else "Pending partner verification ⚠️",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = user.isDriverVerified,
                            onCheckedChange = { onToggleDriverVerification(it) },
                            enabled = !isUpdating
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))
                }

                // Switch Role (CUSTOMER <-> DRIVER only)
                if (user.role != UserRole.ADMIN) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Account Type",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Switch between Rider and Driver Partner",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        JJNOutlinedButton(
                            text = if (user.role == UserRole.CUSTOMER) "Make Driver" else "Make Customer",
                            onClick = {
                                val target = if (user.role == UserRole.CUSTOMER) UserRole.DRIVER else UserRole.CUSTOMER
                                onChangeRole(target)
                            },
                            enabled = !isUpdating
                        )
                    }
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
