package com.example.globalarch.feature.settings

import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.mvi.UiIntent
import com.example.globalarch.core.mvi.UiState

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
) : UiState

sealed interface SettingsIntent : UiIntent {
    data class SelectTheme(
        val mode: ThemeMode,
    ) : SettingsIntent

    data class SetDynamicColor(
        val enabled: Boolean,
    ) : SettingsIntent
}
