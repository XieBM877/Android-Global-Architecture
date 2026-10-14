package com.example.globalarch.feature.detail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.globalarch.feature.detail.NoteDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class NoteDetailNavKey(
    val noteId: Long? = null,
) : NavKey

fun EntryProviderScope<NavKey>.noteDetailEntry(
    onNavigateBack: () -> Unit,
    metadata: Map<String, Any> = emptyMap(),
) {
    entry<NoteDetailNavKey>(metadata = metadata) { key ->
        NoteDetailRoute(noteId = key.noteId, onNavigateBack = onNavigateBack)
    }
}
