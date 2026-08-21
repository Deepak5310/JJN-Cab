package com.deecode.myapp.domain.repository

import com.deecode.myapp.domain.model.AppSettings
import com.deecode.myapp.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
