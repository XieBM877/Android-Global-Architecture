package com.example.globalarch.feature.settings

import androidx.lifecycle.viewModelScope
import com.example.globalarch.core.analytics.AnalyticsEvents
import com.example.globalarch.core.analytics.AnalyticsTracker
import com.example.globalarch.core.domain.repository.SettingsRepository
import com.example.globalarch.core.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analytics: AnalyticsTracker,
) : MviViewModel<SettingsState, SettingsIntent, Nothing>() {
    override val state: StateFlow<SettingsState> = settingsRepository.settings
        .map { settings ->
            SettingsState(
                themeMode = settings.themeMode,
                useDynamicColor = settings.useDynamicColor,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsState(),
        )

    override fun handle(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SelectTheme -> {
                viewModelScope.launch {
                    settingsRepository.setThemeMode(intent.mode)
                    analytics.track(AnalyticsEvents.themeChanged(intent.mode.name))
                }
            }

            is SettingsIntent.SetDynamicColor -> {
                viewModelScope.launch {
                    settingsRepository.setDynamicColor(intent.enabled)
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
