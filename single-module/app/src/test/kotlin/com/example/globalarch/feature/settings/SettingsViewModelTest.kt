package com.example.globalarch.feature.settings

import com.example.globalarch.core.analytics.AnalyticsEvents
import com.example.globalarch.core.analytics.AnalyticsTracker
import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.model.UserSettings
import com.example.globalarch.core.testing.FakeSettingsRepository
import com.example.globalarch.core.testing.MainDispatcherRule
import com.example.globalarch.core.testing.keepCollecting
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository(UserSettings(themeMode = ThemeMode.LIGHT, useDynamicColor = false))
    private val analytics = mockk<AnalyticsTracker>(relaxed = true)

    @Test
    fun state_reflectsStoredSettings() = runTest {
        val viewModel = SettingsViewModel(repository, analytics)
        keepCollecting(viewModel.state)

        assertEquals(SettingsState(themeMode = ThemeMode.LIGHT, useDynamicColor = false), viewModel.state.value)
    }

    @Test
    fun dispatch_selectTheme_persistsAndTracksChange() = runTest {
        val viewModel = SettingsViewModel(repository, analytics)
        keepCollecting(viewModel.state)

        viewModel.dispatch(SettingsIntent.SelectTheme(ThemeMode.DARK))

        assertEquals(ThemeMode.DARK, repository.settings.first().themeMode)
        assertEquals(ThemeMode.DARK, viewModel.state.value.themeMode)
        verify(exactly = 1) { analytics.track(AnalyticsEvents.themeChanged("DARK")) }
    }

    @Test
    fun dispatch_setDynamicColor_persistsWithoutTouchingTheme() = runTest {
        val viewModel = SettingsViewModel(repository, analytics)
        keepCollecting(viewModel.state)

        viewModel.dispatch(SettingsIntent.SetDynamicColor(true))

        assertEquals(UserSettings(themeMode = ThemeMode.LIGHT, useDynamicColor = true), repository.settings.first())
        assertEquals(true, viewModel.state.value.useDynamicColor)
    }
}
