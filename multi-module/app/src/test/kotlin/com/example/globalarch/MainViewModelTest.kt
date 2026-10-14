package com.example.globalarch

import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.model.UserSettings
import com.example.globalarch.core.testing.FakeSettingsRepository
import com.example.globalarch.core.testing.MainDispatcherRule
import com.example.globalarch.core.testing.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_beforeCollection_isLoadingWithDefaults() {
        val viewModel = MainViewModel(FakeSettingsRepository())

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(ThemeMode.SYSTEM, viewModel.state.value.themeMode)
    }

    @Test
    fun state_afterCollection_exposesStoredSettingsAndStopsLoading() = runTest {
        val repository = FakeSettingsRepository(UserSettings(themeMode = ThemeMode.DARK, useDynamicColor = false))
        val viewModel = MainViewModel(repository)
        keepCollecting(viewModel.state)

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(
            MainState(isLoading = false, themeMode = ThemeMode.DARK, useDynamicColor = false),
            viewModel.state.value,
        )
    }

    @Test
    fun state_settingsChange_isReflected() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = MainViewModel(repository)
        keepCollecting(viewModel.state)

        repository.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, viewModel.state.value.themeMode)
    }
}
