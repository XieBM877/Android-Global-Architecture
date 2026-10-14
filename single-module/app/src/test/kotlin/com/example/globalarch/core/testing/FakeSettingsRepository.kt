package com.example.globalarch.core.testing

import com.example.globalarch.core.domain.repository.SettingsRepository
import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(
    initial: UserSettings = UserSettings(),
) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override val settings: Flow<UserSettings> = state

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.update { it.copy(themeMode = mode) }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        state.update { it.copy(useDynamicColor = enabled) }
    }
}
