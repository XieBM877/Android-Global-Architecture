package com.example.globalarch.core.domain.repository

import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDynamicColor(enabled: Boolean)
}
