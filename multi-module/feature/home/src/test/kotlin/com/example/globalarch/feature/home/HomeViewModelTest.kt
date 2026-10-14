package com.example.globalarch.feature.home

import app.cash.turbine.test
import com.example.globalarch.core.analytics.AnalyticsEvents
import com.example.globalarch.core.analytics.AnalyticsTracker
import com.example.globalarch.core.testing.FakeNotesRepository
import com.example.globalarch.core.testing.MainDispatcherRule
import com.example.globalarch.core.testing.TestNotes
import com.example.globalarch.core.testing.keepCollecting
import com.example.globalarch.core.ui.uiText
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNotesRepository(TestNotes.All)
    private val analytics = mockk<AnalyticsTracker>(relaxed = true)

    private fun createViewModel() = HomeViewModel(repository, analytics)

    @Test
    fun state_afterFirstEmission_listsNotesNewestFirstWithPreview() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.state)

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(listOf("Ideas", "Groceries"), state.notes.map(NoteItem::title))
        assertEquals("Milk", state.notes.last().preview)
    }

    @Test
    fun init_triggersSingleRepositoryRefresh() = runTest {
        createViewModel()

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun state_afterInit_isNotRefreshingOnceRefreshCompletes() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.state)

        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun dispatch_refresh_secondTime_callsRepositoryAgain() = runTest {
        val viewModel = createViewModel()

        viewModel.dispatch(HomeIntent.Refresh)

        assertEquals(2, repository.refreshCount)
    }

    @Test
    fun init_refreshFails_emitsShowMessageEffect() = runTest {
        repository.refreshResult = Result.failure(IOException("offline"))
        val viewModel = createViewModel()

        viewModel.effects.test {
            assertEquals(
                HomeEffect.ShowMessage(uiText(R.string.home_error_refresh_failed)),
                awaitItem(),
            )
        }
    }

    @Test
    fun dispatch_deleteNote_removesNoteTracksEventAndEmitsMessage() = runTest {
        val viewModel = createViewModel()
        keepCollecting(viewModel.state)

        viewModel.dispatch(HomeIntent.DeleteNote(TestNotes.Groceries.id))

        assertEquals(
            listOf(TestNotes.Ideas.id),
            viewModel.state.value.notes
                .map(NoteItem::id),
        )
        verify(exactly = 1) { analytics.track(AnalyticsEvents.noteDeleted()) }
        viewModel.effects.test {
            assertEquals(HomeEffect.ShowMessage(uiText(R.string.home_message_deleted)), awaitItem())
        }
    }

    @Test
    fun dispatch_navigationIntents_emitMatchingEffects() = runTest {
        val viewModel = createViewModel()

        viewModel.dispatch(HomeIntent.OpenNote(7))
        viewModel.dispatch(HomeIntent.AddNote)
        viewModel.dispatch(HomeIntent.OpenSettings)

        viewModel.effects.test {
            assertEquals(HomeEffect.NavigateToNote(7), awaitItem())
            assertEquals(HomeEffect.NavigateToNewNote, awaitItem())
            assertEquals(HomeEffect.NavigateToSettings, awaitItem())
        }
    }

    @Test
    fun state_emptyRepository_exposesEmptyListAfterLoading() = runTest {
        val viewModel = HomeViewModel(FakeNotesRepository(), analytics)
        keepCollecting(viewModel.state)

        assertTrue(
            viewModel.state.value.notes
                .isEmpty(),
        )
        assertFalse(viewModel.state.value.isLoading)
    }
}
