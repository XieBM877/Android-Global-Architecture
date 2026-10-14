package com.example.globalarch.feature.settings

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.globalarch.core.designsystem.theme.AppTheme
import com.example.globalarch.core.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<SettingsIntent>()

    private fun show(state: SettingsState) {
        composeRule.setContent {
            AppTheme {
                SettingsScreen(
                    state = state,
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }
    }

    @Test
    fun themeOptions_markCurrentModeAsSelected() {
        show(SettingsState(themeMode = ThemeMode.DARK))

        composeRule.onNodeWithTag(SettingsTags.theme(ThemeMode.DARK)).assertIsSelected()
        composeRule.onNodeWithTag(SettingsTags.theme(ThemeMode.LIGHT)).assertIsDisplayed()
    }

    @Test
    fun themeOptionClick_dispatchesSelectTheme() {
        show(SettingsState(themeMode = ThemeMode.SYSTEM))

        composeRule.onNodeWithTag(SettingsTags.theme(ThemeMode.LIGHT)).performClick()

        assertEquals(listOf<SettingsIntent>(SettingsIntent.SelectTheme(ThemeMode.LIGHT)), intents)
    }

    @Test
    fun dynamicColorToggle_dispatchesSetDynamicColor() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        show(SettingsState(useDynamicColor = true))

        composeRule.onNodeWithTag(SettingsTags.DynamicColor).performClick()

        assertEquals(listOf<SettingsIntent>(SettingsIntent.SetDynamicColor(false)), intents)
    }
}
