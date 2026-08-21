package com.deecode.myapp.feature.settings

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.domain.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val isAboutDialogVisible: Boolean = false,
    val isPrivacySheetVisible: Boolean = false,
    val isTermsSheetVisible: Boolean = false,
    val appVersion: String = "1.0",
    val appBuild: String = "1"
) : UiState

sealed interface SettingsUiEvent : UiEvent {
    data class SetThemeMode(val mode: ThemeMode) : SettingsUiEvent
    data class SetNotificationsEnabled(val enabled: Boolean) : SettingsUiEvent
    data object OpenAboutDialog : SettingsUiEvent
    data object CloseAboutDialog : SettingsUiEvent
    data object OpenPrivacySheet : SettingsUiEvent
    data object ClosePrivacySheet : SettingsUiEvent
    data object OpenTermsSheet : SettingsUiEvent
    data object CloseTermsSheet : SettingsUiEvent
    data object SignOut : SettingsUiEvent
}
