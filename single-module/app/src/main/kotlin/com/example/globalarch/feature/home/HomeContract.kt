package com.example.globalarch.feature.home

import com.example.globalarch.core.model.Note
import com.example.globalarch.core.mvi.UiEffect
import com.example.globalarch.core.mvi.UiIntent
import com.example.globalarch.core.mvi.UiState
import com.example.globalarch.core.ui.UiText

data class NoteItem(
    val id: Long,
    val title: String,
    val preview: String,
)

data class HomeState(
    val notes: List<NoteItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
) : UiState

sealed interface HomeIntent : UiIntent {
    data object Refresh : HomeIntent

    data object AddNote : HomeIntent

    data object OpenSettings : HomeIntent

    data class OpenNote(
        val id: Long,
    ) : HomeIntent

    data class DeleteNote(
        val id: Long,
    ) : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class NavigateToNote(
        val id: Long,
    ) : HomeEffect

    data object NavigateToNewNote : HomeEffect

    data object NavigateToSettings : HomeEffect

    data class ShowMessage(
        val message: UiText,
    ) : HomeEffect
}

internal fun Note.toItem(): NoteItem = NoteItem(
    id = id,
    title = title,
    preview = content.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty(),
)
