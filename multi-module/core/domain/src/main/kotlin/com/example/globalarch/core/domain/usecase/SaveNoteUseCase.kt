package com.example.globalarch.core.domain.usecase

import com.example.globalarch.core.common.suspendRunCatching
import com.example.globalarch.core.domain.repository.NotesRepository
import com.example.globalarch.core.model.Note
import java.time.Clock
import javax.inject.Inject

class NoteValidationException(
    val reason: Reason,
) : IllegalArgumentException(reason.name) {
    enum class Reason {
        EMPTY_TITLE,
        TITLE_TOO_LONG,
        CONTENT_TOO_LONG,
    }
}

class SaveNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        id: Long?,
        title: String,
        content: String,
    ): Result<Long> {
        val normalizedTitle = title.trim()
        val normalizedContent = content.trim()
        val violation = validate(normalizedTitle, normalizedContent)
        if (violation != null) {
            return Result.failure(NoteValidationException(violation))
        }
        val note = Note(
            id = id ?: Note.NEW_ID,
            title = normalizedTitle,
            content = normalizedContent,
            updatedAtMillis = clock.millis(),
        )
        return suspendRunCatching { notesRepository.upsert(note) }
    }

    private fun validate(
        title: String,
        content: String,
    ): NoteValidationException.Reason? = when {
        title.isEmpty() -> NoteValidationException.Reason.EMPTY_TITLE
        title.length > MAX_TITLE_LENGTH -> NoteValidationException.Reason.TITLE_TOO_LONG
        content.length > MAX_CONTENT_LENGTH -> NoteValidationException.Reason.CONTENT_TOO_LONG
        else -> null
    }

    companion object {
        const val MAX_TITLE_LENGTH = 100
        const val MAX_CONTENT_LENGTH = 10_000
    }
}
