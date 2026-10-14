package com.example.globalarch.feature.detail

import app.cash.turbine.test
import com.example.globalarch.core.analytics.AnalyticsEvents
import com.example.globalarch.core.analytics.AnalyticsTracker
import com.example.globalarch.core.domain.usecase.SaveNoteUseCase
import com.example.globalarch.core.testing.FakeNotesRepository
import com.example.globalarch.core.testing.MainDispatcherRule
import com.example.globalarch.core.testing.TestNotes
import com.example.globalarch.core.ui.uiText
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNotesRepository(TestNotes.All)
    private val analytics = mockk<AnalyticsTracker>(relaxed = true)
    private val clock = Clock.fixed(Instant.ofEpochMilli(5_000), ZoneOffset.UTC)

    private fun createViewModel(noteId: Long?) = DetailViewModel(
        noteId = noteId,
        notesRepository = repository,
        saveNote = SaveNoteUseCase(repository, clock),
        analytics = analytics,
    )

    @Test
    fun init_newNote_startsWithEmptyFormThatIsNotLoading() {
        val state = createViewModel(noteId = null).state.value

        assertTrue(state.isNew)
        assertFalse(state.isLoading)
        assertEquals("", state.title)
    }

    @Test
    fun init_existingNote_loadsTitleAndContent() {
        val state = createViewModel(TestNotes.Groceries.id).state.value

        assertFalse(state.isNew)
        assertFalse(state.isLoading)
        assertEquals(TestNotes.Groceries.title, state.title)
        assertEquals(TestNotes.Groceries.content, state.content)
    }

    @Test
    fun init_missingNote_marksNotFound() {
        val state = createViewModel(noteId = 999).state.value

        assertTrue(state.isNotFound)
        assertFalse(state.isLoading)
    }

    @Test
    fun dispatch_titleChanged_updatesStateAndClearsPreviousError() {
        val viewModel = createViewModel(noteId = null)
        viewModel.dispatch(DetailIntent.Save)
        assertNotNull(viewModel.state.value.titleError)

        viewModel.dispatch(DetailIntent.TitleChanged("Hello"))

        assertEquals("Hello", viewModel.state.value.title)
        assertNull(viewModel.state.value.titleError)
    }

    @Test
    fun dispatch_save_blankTitle_setsTitleErrorAndStaysOnScreen() = runTest {
        val viewModel = createViewModel(noteId = null)

        viewModel.dispatch(DetailIntent.Save)

        assertEquals(uiText(R.string.detail_error_title_empty), viewModel.state.value.titleError)
        assertFalse(viewModel.state.value.isSaving)
        assertTrue(repository.currentNotes().size == TestNotes.All.size)
        viewModel.effects.test { expectNoEvents() }
    }

    @Test
    fun dispatch_save_validNewNote_persistsTracksAndNavigatesBack() = runTest {
        val viewModel = createViewModel(noteId = null)
        viewModel.dispatch(DetailIntent.TitleChanged("  Plan  "))
        viewModel.dispatch(DetailIntent.ContentChanged("Write tests"))

        viewModel.dispatch(DetailIntent.Save)

        val saved = repository.currentNotes().single { it.title == "Plan" }
        assertEquals("Write tests", saved.content)
        assertEquals(5_000L, saved.updatedAtMillis)
        verify(exactly = 1) { analytics.track(AnalyticsEvents.noteSaved(isNew = true)) }
        viewModel.effects.test { assertEquals(DetailEffect.NavigateBack, awaitItem()) }
    }

    @Test
    fun dispatch_save_existingNote_updatesInPlace() = runTest {
        val viewModel = createViewModel(TestNotes.Ideas.id)
        viewModel.dispatch(DetailIntent.TitleChanged("Ideas v2"))

        viewModel.dispatch(DetailIntent.Save)

        assertEquals(TestNotes.All.size, repository.currentNotes().size)
        assertEquals("Ideas v2", repository.currentNotes().single { it.id == TestNotes.Ideas.id }.title)
        verify(exactly = 1) { analytics.track(AnalyticsEvents.noteSaved(isNew = false)) }
    }

    @Test
    fun dispatch_save_repositoryFails_showsMessageAndKeepsForm() = runTest {
        repository.upsertFailure = IllegalStateException("disk full")
        val viewModel = createViewModel(noteId = null)
        viewModel.dispatch(DetailIntent.TitleChanged("Keep me"))

        viewModel.dispatch(DetailIntent.Save)

        assertEquals("Keep me", viewModel.state.value.title)
        assertFalse(viewModel.state.value.isSaving)
        viewModel.effects.test {
            assertEquals(DetailEffect.ShowMessage(uiText(R.string.detail_error_save_failed)), awaitItem())
        }
    }

    @Test
    fun dispatch_delete_existingNote_removesTracksAndNavigatesBack() = runTest {
        val viewModel = createViewModel(TestNotes.Groceries.id)

        viewModel.dispatch(DetailIntent.Delete)

        assertTrue(repository.currentNotes().none { it.id == TestNotes.Groceries.id })
        verify(exactly = 1) { analytics.track(AnalyticsEvents.noteDeleted()) }
        viewModel.effects.test { assertEquals(DetailEffect.NavigateBack, awaitItem()) }
    }

    @Test
    fun dispatch_delete_newNote_isIgnored() = runTest {
        val viewModel = createViewModel(noteId = null)

        viewModel.dispatch(DetailIntent.Delete)

        assertEquals(TestNotes.All.size, repository.currentNotes().size)
        viewModel.effects.test { expectNoEvents() }
    }
}
