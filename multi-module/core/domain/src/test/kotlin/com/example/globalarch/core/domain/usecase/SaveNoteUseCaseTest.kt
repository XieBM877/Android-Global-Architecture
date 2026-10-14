package com.example.globalarch.core.domain.usecase

import com.example.globalarch.core.model.Note
import com.example.globalarch.core.testing.FakeNotesRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveNoteUseCaseTest {
    private val fixedClock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000), ZoneOffset.UTC)
    private val repository = FakeNotesRepository()
    private val saveNote = SaveNoteUseCase(repository, fixedClock)

    @Test
    fun invoke_blankTitle_failsWithEmptyTitleAndStoresNothing() = runTest {
        val result = saveNote(id = null, title = "   ", content = "body")

        val failure = result.exceptionOrNull() as NoteValidationException
        assertEquals(NoteValidationException.Reason.EMPTY_TITLE, failure.reason)
        assertTrue(repository.currentNotes().isEmpty())
    }

    @Test
    fun invoke_titleOverLimit_failsWithTitleTooLong() = runTest {
        val longTitle = "a".repeat(SaveNoteUseCase.MAX_TITLE_LENGTH + 1)

        val result = saveNote(id = null, title = longTitle, content = "")

        val failure = result.exceptionOrNull() as NoteValidationException
        assertEquals(NoteValidationException.Reason.TITLE_TOO_LONG, failure.reason)
    }

    @Test
    fun invoke_contentOverLimit_failsWithContentTooLong() = runTest {
        val longContent = "a".repeat(SaveNoteUseCase.MAX_CONTENT_LENGTH + 1)

        val result = saveNote(id = null, title = "title", content = longContent)

        val failure = result.exceptionOrNull() as NoteValidationException
        assertEquals(NoteValidationException.Reason.CONTENT_TOO_LONG, failure.reason)
    }

    @Test
    fun invoke_validNewNote_trimsFieldsStampsClockAndReturnsGeneratedId() = runTest {
        val result = saveNote(id = null, title = "  Shopping  ", content = "  milk  ")

        val savedId = result.getOrThrow()
        val saved = repository.currentNotes().single()
        assertEquals(savedId, saved.id)
        assertEquals(
            Note(id = savedId, title = "Shopping", content = "milk", updatedAtMillis = 1_700_000_000_000),
            saved,
        )
    }

    @Test
    fun invoke_existingId_updatesInsteadOfCreating() = runTest {
        val firstId = saveNote(id = null, title = "Old", content = "").getOrThrow()

        saveNote(id = firstId, title = "New", content = "changed").getOrThrow()

        val saved = repository.currentNotes().single()
        assertEquals(firstId, saved.id)
        assertEquals("New", saved.title)
    }
}
