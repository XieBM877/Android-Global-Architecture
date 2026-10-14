package com.example.globalarch

import androidx.lifecycle.viewModelScope
import com.example.globalarch.core.domain.repository.SettingsRepository
import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.mvi.MviViewModel
import com.example.globalarch.core.mvi.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MainState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
) : UiState

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : MviViewModel<MainState, Nothing, Nothing>() {
    override val state: StateFlow<MainState> = settingsRepository.settings
        .map { settings ->
            MainState(
                isLoading = false,
                themeMode = settings.themeMode,
                useDynamicColor = settings.useDynamicColor,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = MainState(),
        )

    override fun handle(intent: Nothing) = Unit

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
