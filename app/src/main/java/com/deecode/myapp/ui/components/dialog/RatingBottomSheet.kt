package com.deecode.myapp.ui.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    title: String = "Rate your Ride",
    subtitle: String = "How was your experience?",
    targetName: String? = null,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, review: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    val ratingLabels = listOf(
        "1" to "Poor 🙁",
        "2" to "Fair 😐",
        "3" to "Good 🙂",
        "4" to "Very Good 😊",
        "5" to "Excellent! 🌟"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(bottom = MaterialTheme.spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (!targetName.isNullOrBlank()) "How was your experience with $targetName?" else subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // 5-Star Interactive Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(44.dp)
                            .clickable(enabled = !isSubmitting) {
                                selectedRating = i
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (i <= selectedRating) "★" else "☆",
                            fontSize = 36.sp,
                            color = if (i <= selectedRating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating Mood Label
            Text(
                text = ratingLabels.getOrNull(selectedRating - 1)?.second ?: "Excellent! 🌟",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Review note input
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Write a note (optional)") },
                placeholder = { Text("Friendly driver, smooth ride...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                enabled = !isSubmitting
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            JJNPrimaryButton(
                text = "Submit Rating",
                onClick = { onSubmit(selectedRating, reviewText.ifBlank { null }) },
                isLoading = isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text(
                    text = "Not Now",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
