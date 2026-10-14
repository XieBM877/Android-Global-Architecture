package com.example.globalarch.core.data

import com.example.globalarch.core.database.NoteDao
import com.example.globalarch.core.database.asEntity
import com.example.globalarch.core.database.asModel
import com.example.globalarch.core.domain.repository.NotesRepository
import com.example.globalarch.core.model.Note
import com.example.globalarch.core.network.NotesRemoteDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineFirstNotesRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val remote: NotesRemoteDataSource,
) : NotesRepository {
    override fun observeNotes(): Flow<List<Note>> = noteDao.observeAll().map { entities ->
        entities.map { it.asModel() }
    }

    override fun observeNote(id: Long): Flow<Note?> = noteDao.observeById(id).map { it?.asModel() }

    override suspend fun upsert(note: Note): Long {
        val generatedId = noteDao.upsert(note.asEntity())
        return if (generatedId == UPDATED_ROW) note.id else generatedId
    }

    override suspend fun delete(id: Long) {
        noteDao.deleteById(id)
    }

    override suspend fun refresh(): Result<Unit> = remote.fetchNotes().map { notes ->
        noteDao.upsertAll(notes.map(Note::asEntity))
    }

    private companion object {
        const val UPDATED_ROW = -1L
    }
}
