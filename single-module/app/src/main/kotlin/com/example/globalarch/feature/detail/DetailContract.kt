package com.example.globalarch.feature.detail

import com.example.globalarch.core.mvi.UiEffect
import com.example.globalarch.core.mvi.UiIntent
import com.example.globalarch.core.mvi.UiState
import com.example.globalarch.core.ui.UiText

data class DetailState(
    val title: String = "",
    val content: String = "",
    val isNew: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNotFound: Boolean = false,
    val titleError: UiText? = null,
    val contentError: UiText? = null,
) : UiState

sealed interface DetailIntent : UiIntent {
    data class TitleChanged(
        val value: String,
    ) : DetailIntent

    data class ContentChanged(
        val value: String,
    ) : DetailIntent

    data object Save : DetailIntent

    data object Delete : DetailIntent
}

sealed interface DetailEffect : UiEffect {
    data object NavigateBack : DetailEffect

    data class ShowMessage(
        val message: UiText,
    ) : DetailEffect
}
