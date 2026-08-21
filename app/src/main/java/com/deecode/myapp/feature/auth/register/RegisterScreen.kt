package com.deecode.myapp.feature.auth.register

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.components.JJNTextField
import com.deecode.myapp.ui.theme.spacing

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToCustomer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collect { effect ->
            when (effect) {
                is RegisterNavigationEffect.Success -> {
                    onNavigateToCustomer()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Join JJN-Cab for fast and reliable rides",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // General Error Banner
            if (!uiState.generalError.isNullOrBlank()) {
                JJNCard(
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = uiState.generalError ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Name Input
            JJNTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.OnNameChange(it)) },
                label = "Full Name",
                placeholder = "John Doe",
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Email Input
            JJNTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.OnEmailChange(it)) },
                label = "Email Address",
                placeholder = "name@example.com",
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Phone Input
            JJNTextField(
                value = uiState.phone,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.OnPhoneChange(it)) },
                label = "Phone Number",
                placeholder = "+91 9876543210",
                isError = uiState.phoneError != null,
                errorMessage = uiState.phoneError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Password Input
            JJNTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.OnPasswordChange(it)) },
                label = "Password",
                placeholder = "Min 6 characters",
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    Text(
                        text = if (uiState.isPasswordVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable { viewModel.onEvent(RegisterUiEvent.OnTogglePasswordVisibility) }
                            .padding(8.dp)
                    )
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Confirm Password Input
            JJNTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onEvent(RegisterUiEvent.OnConfirmPasswordChange(it)) },
                label = "Confirm Password",
                placeholder = "Re-enter your password",
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onEvent(RegisterUiEvent.OnRegisterClick)
                    }
                ),
                trailingIcon = {
                    Text(
                        text = if (uiState.isConfirmPasswordVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable { viewModel.onEvent(RegisterUiEvent.OnToggleConfirmPasswordVisibility) }
                            .padding(8.dp)
                    )
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Submit Button
            JJNPrimaryButton(
                text = "Create Account",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onEvent(RegisterUiEvent.OnRegisterClick)
                },
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}
