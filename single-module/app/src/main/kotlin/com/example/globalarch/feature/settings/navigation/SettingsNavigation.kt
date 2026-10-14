package com.example.globalarch.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.globalarch.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsNavKey : NavKey

fun EntryProviderScope<NavKey>.settingsEntry(
    onNavigateBack: () -> Unit,
    metadata: Map<String, Any> = emptyMap(),
) {
    entry<SettingsNavKey>(metadata = metadata) {
        SettingsRoute(onNavigateBack = onNavigateBack)
    }
}
