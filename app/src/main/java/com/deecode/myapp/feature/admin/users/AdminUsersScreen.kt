package com.deecode.myapp.feature.admin.users

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNLoadingIndicator
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNOutlinedCard
import com.deecode.myapp.ui.theme.spacing
import java.util.Locale

@Composable
fun AdminUsersScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading && uiState.allUsers.isEmpty()) {
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

    val customerCount = uiState.allUsers.count { it.role == UserRole.CUSTOMER }
    val driverCount = uiState.allUsers.count { it.role == UserRole.DRIVER }

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
                    text = "User Directory 👥",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${uiState.allUsers.size} registered accounts",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onEvent(AdminUsersUiEvent.SearchQueryChanged(it)) },
            label = { Text("Search by name, email, phone...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { viewModel.onEvent(AdminUsersUiEvent.SearchQueryChanged("")) }
                            .padding(8.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            FilterChip(
                selected = uiState.selectedFilter == UserRoleFilter.ALL,
                onClick = { viewModel.onEvent(AdminUsersUiEvent.SelectFilter(UserRoleFilter.ALL)) },
                label = { Text("All (${uiState.allUsers.size})") }
            )

            FilterChip(
                selected = uiState.selectedFilter == UserRoleFilter.CUSTOMER,
                onClick = { viewModel.onEvent(AdminUsersUiEvent.SelectFilter(UserRoleFilter.CUSTOMER)) },
                label = { Text("Riders ($customerCount)") }
            )

            FilterChip(
                selected = uiState.selectedFilter == UserRoleFilter.DRIVER,
                onClick = { viewModel.onEvent(AdminUsersUiEvent.SelectFilter(UserRoleFilter.DRIVER)) },
                label = { Text("Drivers ($driverCount)") }
            )
        }

        // Action / Error Messages
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
                            .clickable { viewModel.onEvent(AdminUsersUiEvent.ClearActionMessage) }
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
                        onClick = { viewModel.onEvent(AdminUsersUiEvent.Refresh) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Filtered Users List
        if (uiState.filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) "No users matching '${uiState.searchQuery}'" else "No users registered.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(uiState.filteredUsers, key = { it.uid }) { user ->
                    AdminUserItemCard(
                        user = user,
                        onClick = { viewModel.onEvent(AdminUsersUiEvent.SelectUser(user)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }
            }
        }
    }

    // User Detail Bottom Sheet
    uiState.selectedUser?.let { selectedUser ->
        UserDetailBottomSheet(
            user = selectedUser,
            isUpdating = uiState.isUpdating,
            onDismiss = { viewModel.onEvent(AdminUsersUiEvent.SelectUser(null)) },
            onToggleActive = { isActive ->
                viewModel.onEvent(AdminUsersUiEvent.ToggleUserActive(selectedUser.uid, isActive))
            },
            onToggleDriverVerification = { isVerified ->
                viewModel.onEvent(AdminUsersUiEvent.ToggleDriverVerification(selectedUser.uid, isVerified))
            },
            onChangeRole = { newRole ->
                viewModel.onEvent(AdminUsersUiEvent.ChangeUserRole(selectedUser.uid, newRole))
            }
        )
    }
}

@Composable
private fun AdminUserItemCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    JJNOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = MaterialTheme.spacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase().ifBlank { "U" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (user.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name.ifBlank { "Unnamed User" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = user.role.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1
                )

                if (user.phone.isNotBlank()) {
                    Text(
                        text = user.phone,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (user.isActive) "Active 🟢" else "Disabled 🔴",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                if (user.role == UserRole.DRIVER) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (user.isDriverVerified) "Verified ✅" else "Pending ⚠️",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = if (user.isDriverVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    )
                }

                if (user.ratingCount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⭐ ${String.format(Locale.US, "%.1f", user.ratingAverage)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
