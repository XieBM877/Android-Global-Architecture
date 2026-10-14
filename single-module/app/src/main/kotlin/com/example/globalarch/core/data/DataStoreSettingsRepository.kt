package com.example.globalarch.core.data

import com.example.globalarch.core.datastore.SettingsDataSource
import com.example.globalarch.core.domain.repository.SettingsRepository
import com.example.globalarch.core.model.ThemeMode
import com.example.globalarch.core.model.UserSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class DataStoreSettingsRepository @Inject constructor(
    private val dataSource: SettingsDataSource,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = dataSource.settings

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataSource.setThemeMode(mode)
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataSource.setDynamicColor(enabled)
    }
}
