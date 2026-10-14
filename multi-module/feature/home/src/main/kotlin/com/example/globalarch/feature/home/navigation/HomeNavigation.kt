package com.example.globalarch.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.globalarch.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : NavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onOpenNote: (Long) -> Unit,
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit,
    selectedNoteId: Long? = null,
    metadata: Map<String, Any> = emptyMap(),
) {
    entry<HomeNavKey>(metadata = metadata) {
        HomeRoute(
            onOpenNote = onOpenNote,
            onAddNote = onAddNote,
            onOpenSettings = onOpenSettings,
            selectedNoteId = selectedNoteId,
        )
    }
}
