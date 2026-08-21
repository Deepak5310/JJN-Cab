package com.deecode.myapp.feature.settings

import com.deecode.myapp.domain.model.AppSettings
import com.deecode.myapp.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    @Test
    fun `verifies ThemeMode enum values and fallback parsing`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))

        val invalidParsed = try {
            ThemeMode.valueOf("UNKNOWN_THEME")
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        assertEquals(ThemeMode.SYSTEM, invalidParsed)
    }

    @Test
    fun `verifies AppSettings default values`() {
        val settings = AppSettings()
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun `verifies SettingsUiState toggles and defaults`() {
        val state = SettingsUiState(
            themeMode = ThemeMode.DARK,
            notificationsEnabled = false,
            appVersion = "1.0.0",
            appBuild = "10"
        )

        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals(false, state.notificationsEnabled)
        assertEquals("1.0.0", state.appVersion)
        assertEquals("10", state.appBuild)
    }
}
