package com.example.globalarch.feature.detail

import androidx.lifecycle.viewModelScope
import com.example.globalarch.R
import com.example.globalarch.core.analytics.AnalyticsEvents
import com.example.globalarch.core.analytics.AnalyticsTracker
import com.example.globalarch.core.domain.repository.NotesRepository
import com.example.globalarch.core.domain.usecase.NoteValidationException
import com.example.globalarch.core.domain.usecase.SaveNoteUseCase
import com.example.globalarch.core.mvi.SelfOwnedMviViewModel
import com.example.globalarch.core.ui.uiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val noteId: Long?,
    private val notesRepository: NotesRepository,
    private val saveNote: SaveNoteUseCase,
    private val analytics: AnalyticsTracker,
) : SelfOwnedMviViewModel<DetailState, DetailIntent, DetailEffect>(
    DetailState(isNew = noteId == null, isLoading = noteId != null),
) {
    @AssistedFactory
    interface Factory {
        fun create(noteId: Long?): DetailViewModel
    }

    init {
        if (noteId != null) load(noteId)
    }

    override fun handle(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.TitleChanged -> reduce { it.copy(title = intent.value, titleError = null) }
            is DetailIntent.ContentChanged -> reduce { it.copy(content = intent.value, contentError = null) }
            DetailIntent.Save -> save()
            DetailIntent.Delete -> delete()
        }
    }

    private fun load(id: Long) {
        viewModelScope.launch {
            val note = notesRepository.observeNote(id).first()
            if (note == null) {
                reduce { it.copy(isLoading = false, isNotFound = true) }
            } else {
                reduce { it.copy(title = note.title, content = note.content, isLoading = false) }
            }
        }
    }

    private fun save() {
        val snapshot = currentState
        if (snapshot.isSaving || snapshot.isLoading || snapshot.isNotFound) return
        reduce { it.copy(isSaving = true) }
        viewModelScope.launch {
            saveNote(noteId, snapshot.title, snapshot.content)
                .onSuccess {
                    analytics.track(AnalyticsEvents.noteSaved(isNew = noteId == null))
                    reduce { it.copy(isSaving = false) }
                    sendEffect(DetailEffect.NavigateBack)
                }.onFailure { failure -> onSaveFailed(failure) }
        }
    }

    private fun onSaveFailed(failure: Throwable) {
        if (failure is NoteValidationException) {
            reduce { it.copy(isSaving = false).withViolation(failure.reason) }
        } else {
            reduce { it.copy(isSaving = false) }
            sendEffect(DetailEffect.ShowMessage(uiText(R.string.detail_error_save_failed)))
        }
    }

    private fun delete() {
        val id = noteId ?: return
        viewModelScope.launch {
            notesRepository.delete(id)
            analytics.track(AnalyticsEvents.noteDeleted())
            sendEffect(DetailEffect.NavigateBack)
        }
    }
}

private fun DetailState.withViolation(reason: NoteValidationException.Reason): DetailState = when (reason) {
    NoteValidationException.Reason.EMPTY_TITLE -> {
        copy(titleError = uiText(R.string.detail_error_title_empty))
    }

    NoteValidationException.Reason.TITLE_TOO_LONG -> {
        copy(titleError = uiText(R.string.detail_error_title_too_long, SaveNoteUseCase.MAX_TITLE_LENGTH))
    }

    NoteValidationException.Reason.CONTENT_TOO_LONG -> {
        copy(contentError = uiText(R.string.detail_error_content_too_long, SaveNoteUseCase.MAX_CONTENT_LENGTH))
    }
}
