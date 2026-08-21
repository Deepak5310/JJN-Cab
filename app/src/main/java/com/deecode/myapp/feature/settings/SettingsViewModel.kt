package com.deecode.myapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deecode.myapp.BuildConfig
import com.deecode.myapp.domain.model.ThemeMode
import com.deecode.myapp.domain.repository.AuthRepository
import com.deecode.myapp.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appVersion = BuildConfig.VERSION_NAME.ifBlank { "1.0" },
            appBuild = BuildConfig.VERSION_CODE.toString()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        themeMode = settings.themeMode,
                        notificationsEnabled = settings.notificationsEnabled
                    )
                }
            }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SetThemeMode -> {
                viewModelScope.launch {
                    settingsRepository.setThemeMode(event.mode)
                }
            }
            is SettingsUiEvent.SetNotificationsEnabled -> {
                viewModelScope.launch {
                    settingsRepository.setNotificationsEnabled(event.enabled)
                }
            }
            is SettingsUiEvent.OpenAboutDialog -> _uiState.update { it.copy(isAboutDialogVisible = true) }
            is SettingsUiEvent.CloseAboutDialog -> _uiState.update { it.copy(isAboutDialogVisible = false) }
            is SettingsUiEvent.OpenPrivacySheet -> _uiState.update { it.copy(isPrivacySheetVisible = true) }
            is SettingsUiEvent.ClosePrivacySheet -> _uiState.update { it.copy(isPrivacySheetVisible = false) }
            is SettingsUiEvent.OpenTermsSheet -> _uiState.update { it.copy(isTermsSheetVisible = true) }
            is SettingsUiEvent.CloseTermsSheet -> _uiState.update { it.copy(isTermsSheetVisible = false) }
            is SettingsUiEvent.SignOut -> Unit
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }
}
