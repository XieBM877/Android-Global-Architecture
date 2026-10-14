package com.example.globalarch.core.testing

import com.example.globalarch.core.domain.repository.NotesRepository
import com.example.globalarch.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeNotesRepository(
    initialNotes: List<Note> = emptyList(),
) : NotesRepository {
    private val notes = MutableStateFlow(initialNotes)
    private var nextId = (initialNotes.maxOfOrNull(Note::id) ?: 0L) + 1L

    var refreshResult: Result<Unit> = Result.success(Unit)
    var upsertFailure: Throwable? = null
    var refreshCount: Int = 0
        private set

    override fun observeNotes(): Flow<List<Note>> = notes.map { list ->
        list.sortedByDescending(Note::updatedAtMillis)
    }

    override fun observeNote(id: Long): Flow<Note?> = notes.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsert(note: Note): Long {
        upsertFailure?.let { throw it }
        val id = if (note.id == Note.NEW_ID) nextId++ else note.id
        val stored = note.copy(id = id)
        notes.update { list -> list.filterNot { it.id == id } + stored }
        return id
    }

    override suspend fun delete(id: Long) {
        notes.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        return refreshResult
    }

    fun currentNotes(): List<Note> = notes.value
}
