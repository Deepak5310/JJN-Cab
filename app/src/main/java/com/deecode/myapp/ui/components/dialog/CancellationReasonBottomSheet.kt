package com.deecode.myapp.ui.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deecode.myapp.ui.components.JJNOutlinedButton
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.components.JJNTextField
import com.deecode.myapp.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancellationReasonBottomSheet(
    title: String = "Cancel Ride",
    reasons: List<String>,
    isLoading: Boolean = false,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedReason by remember { mutableStateOf(reasons.firstOrNull() ?: "Changed plans") }
    var customReason by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(bottom = MaterialTheme.spacing.extraLarge)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Please select a reason for cancelling this trip:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            reasons.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isLoading) { selectedReason = reason }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedReason == reason),
                        onClick = { if (!isLoading) selectedReason = reason },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.error,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedReason == reason) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (selectedReason.contains("Other", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                JJNTextField(
                    value = customReason,
                    onValueChange = { customReason = it },
                    label = "Specify reason",
                    placeholder = "Describe your reason...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                JJNOutlinedButton(
                    text = "Keep Ride",
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                )

                JJNPrimaryButton(
                    text = if (isLoading) "Cancelling..." else "Confirm Cancel",
                    onClick = {
                        val finalReason = if (selectedReason.contains("Other", ignoreCase = true) && customReason.isNotBlank()) {
                            customReason.trim()
                        } else {
                            selectedReason
                        }
                        onConfirm(finalReason)
                    },
                    enabled = !isLoading,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
