package com.deecode.myapp.feature.auth.login

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.ui.components.JJNCard
import com.deecode.myapp.ui.components.JJNPrimaryButton
import com.deecode.myapp.ui.components.JJNTextField
import com.deecode.myapp.ui.theme.spacing

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToCustomer: () -> Unit,
    onNavigateToDriver: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collect { effect ->
            when (effect) {
                is LoginNavigationEffect.Success -> {
                    when (effect.role) {
                        UserRole.CUSTOMER -> onNavigateToCustomer()
                        UserRole.DRIVER -> onNavigateToDriver()
                        UserRole.ADMIN -> onNavigateToAdmin()
                    }
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
            // App Branding Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JJN",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Sign in to continue your ride",
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

            // Email Input
            JJNTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(LoginUiEvent.OnEmailChange(it)) },
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

            // Password Input
            JJNTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(LoginUiEvent.OnPasswordChange(it)) },
                label = "Password",
                placeholder = "Enter your password",
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onEvent(LoginUiEvent.OnLoginClick)
                    }
                ),
                trailingIcon = {
                    Text(
                        text = if (uiState.isPasswordVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable { viewModel.onEvent(LoginUiEvent.OnTogglePasswordVisibility) }
                            .padding(8.dp)
                    )
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Submit Button
            JJNPrimaryButton(
                text = "Sign In",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onEvent(LoginUiEvent.OnLoginClick)
                },
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Register Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                        onNavigateToRegister()
                    }
                )
            }
        }
    }
}
