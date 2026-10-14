package com.example.globalarch.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.globalarch.feature.detail.navigation.NoteDetailNavKey
import com.example.globalarch.feature.detail.navigation.noteDetailEntry
import com.example.globalarch.feature.home.navigation.HomeNavKey
import com.example.globalarch.feature.home.navigation.homeEntry
import com.example.globalarch.feature.settings.navigation.SettingsNavKey
import com.example.globalarch.feature.settings.navigation.settingsEntry

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeNavKey)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val selectedNoteId = (backStack.lastOrNull() as? NoteDetailNavKey)?.noteId

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider<NavKey> {
            homeEntry(
                onOpenNote = { noteId -> backStack.openNote(noteId) },
                onAddNote = { backStack.openNote(null) },
                onOpenSettings = { backStack.add(SettingsNavKey) },
                selectedNoteId = selectedNoteId,
                metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { DetailPlaceholder() }),
            )
            noteDetailEntry(
                onNavigateBack = { backStack.removeLastOrNull() },
                metadata = ListDetailSceneStrategy.detailPane(),
            )
            settingsEntry(onNavigateBack = { backStack.removeLastOrNull() })
        },
    )
}

private fun NavBackStack<NavKey>.openNote(noteId: Long?) {
    val key = NoteDetailNavKey(noteId)
    if (lastOrNull() is NoteDetailNavKey) {
        set(lastIndex, key)
    } else {
        add(key)
    }
}
