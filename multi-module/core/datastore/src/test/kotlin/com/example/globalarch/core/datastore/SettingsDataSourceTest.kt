package com.example.globalarch.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.model.UserSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsDataSourceTest {
    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private fun TestScope.createDataSource(): SettingsDataSource {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { temporaryFolder.newFile("test.preferences_pb") },
        )
        return SettingsDataSource(dataStore)
    }

    @Test
    fun settings_withoutStoredValues_emitsDefaults() = runTest {
        val dataSource = createDataSource()

        assertEquals(UserSettings(), dataSource.settings.first())
    }

    @Test
    fun setThemeMode_persistsAndKeepsOtherFields() = runTest {
        val dataSource = createDataSource()

        dataSource.setThemeMode(ThemeMode.DARK)

        assertEquals(UserSettings(themeMode = ThemeMode.DARK), dataSource.settings.first())
    }

    @Test
    fun setDynamicColor_persistsAndKeepsOtherFields() = runTest {
        val dataSource = createDataSource()
        dataSource.setThemeMode(ThemeMode.LIGHT)

        dataSource.setDynamicColor(false)

        val expected = UserSettings(themeMode = ThemeMode.LIGHT, useDynamicColor = false)
        assertEquals(expected, dataSource.settings.first())
    }
}
