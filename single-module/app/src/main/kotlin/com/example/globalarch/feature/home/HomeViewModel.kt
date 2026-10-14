package com.example.globalarch.feature.home

import androidx.lifecycle.viewModelScope
import com.example.globalarch.R
import com.example.globalarch.core.analytics.AnalyticsEvents
import com.example.globalarch.core.analytics.AnalyticsTracker
import com.example.globalarch.core.domain.repository.NotesRepository
import com.example.globalarch.core.model.Note
import com.example.globalarch.core.mvi.MviViewModel
import com.example.globalarch.core.ui.uiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val analytics: AnalyticsTracker,
) : MviViewModel<HomeState, HomeIntent, HomeEffect>() {
    private val isRefreshing = MutableStateFlow(false)

    override val state: StateFlow<HomeState> = combine(
        notesRepository.observeNotes(),
        isRefreshing,
    ) { notes, refreshing ->
        HomeState(
            notes = notes.map(Note::toItem),
            isLoading = false,
            isRefreshing = refreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeState(),
    )

    init {
        refresh()
    }

    override fun handle(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Refresh -> refresh()
            HomeIntent.AddNote -> sendEffect(HomeEffect.NavigateToNewNote)
            HomeIntent.OpenSettings -> sendEffect(HomeEffect.NavigateToSettings)
            is HomeIntent.OpenNote -> sendEffect(HomeEffect.NavigateToNote(intent.id))
            is HomeIntent.DeleteNote -> delete(intent.id)
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return
        isRefreshing.value = true
        viewModelScope.launch {
            val result = notesRepository.refresh()
            isRefreshing.value = false
            result.onFailure {
                sendEffect(HomeEffect.ShowMessage(uiText(R.string.home_error_refresh_failed)))
            }
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            notesRepository.delete(id)
            analytics.track(AnalyticsEvents.noteDeleted())
            sendEffect(HomeEffect.ShowMessage(uiText(R.string.home_message_deleted)))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
