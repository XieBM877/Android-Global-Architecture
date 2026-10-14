package com.example.globalarch.core.data

import com.example.globalarch.core.database.NoteDao
import com.example.globalarch.core.database.NoteEntity
import com.example.globalarch.core.model.Note
import com.example.globalarch.core.network.NetworkException
import com.example.globalarch.core.network.NotesRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class InMemoryNoteDao : NoteDao {
    private val rows = MutableStateFlow<List<NoteEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<NoteEntity>> =
        rows.map { list -> list.sortedByDescending { it.updatedAtMillis } }

    override fun observeById(id: Long): Flow<NoteEntity?> = rows.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsert(entity: NoteEntity): Long {
        val isInsert = entity.id == 0L
        val id = if (isInsert) nextId++ else entity.id
        rows.update { list -> list.filterNot { it.id == id } + entity.copy(id = id) }
        return if (isInsert) id else -1L
    }

    override suspend fun upsertAll(entities: List<NoteEntity>) {
        entities.forEach { upsert(it) }
    }

    override suspend fun deleteById(id: Long) {
        rows.update { list -> list.filterNot { it.id == id } }
    }
}

private class StubRemote(
    var result: Result<List<Note>> = Result.success(emptyList()),
) : NotesRemoteDataSource {
    override suspend fun fetchNotes(): Result<List<Note>> = result
}

class OfflineFirstNotesRepositoryTest {
    private val dao = InMemoryNoteDao()
    private val remote = StubRemote()
    private val repository = OfflineFirstNotesRepository(dao, remote)

    @Test
    fun upsert_newNote_returnsGeneratedId() = runTest {
        val id = repository.upsert(Note(id = Note.NEW_ID, title = "t", content = "c", updatedAtMillis = 1))

        assertEquals(1L, id)
        assertEquals("t", repository.observeNote(id).first()?.title)
    }

    @Test
    fun upsert_existingNote_returnsSameIdInsteadOfRoomSentinel() = runTest {
        val id = repository.upsert(Note(id = Note.NEW_ID, title = "old", content = "", updatedAtMillis = 1))

        val updatedId = repository.upsert(Note(id = id, title = "new", content = "", updatedAtMillis = 2))

        assertEquals(id, updatedId)
        assertEquals("new", repository.observeNote(id).first()?.title)
    }

    @Test
    fun observeNotes_ordersNewestFirst() = runTest {
        repository.upsert(Note(id = Note.NEW_ID, title = "older", content = "", updatedAtMillis = 1))
        repository.upsert(Note(id = Note.NEW_ID, title = "newer", content = "", updatedAtMillis = 9))

        assertEquals(listOf("newer", "older"), repository.observeNotes().first().map(Note::title))
    }

    @Test
    fun delete_removesNote() = runTest {
        val id = repository.upsert(Note(id = Note.NEW_ID, title = "gone", content = "", updatedAtMillis = 1))

        repository.delete(id)

        assertTrue(repository.observeNotes().first().isEmpty())
    }

    @Test
    fun refresh_remoteSuccess_storesRemoteNotesLocally() = runTest {
        remote.result = Result.success(listOf(Note(id = 50, title = "remote", content = "", updatedAtMillis = 3)))

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        assertEquals(listOf("remote"), repository.observeNotes().first().map(Note::title))
    }

    @Test
    fun refresh_remoteFailure_returnsFailureAndKeepsLocalNotes() = runTest {
        repository.upsert(Note(id = Note.NEW_ID, title = "local", content = "", updatedAtMillis = 1))
        remote.result = Result.failure(NetworkException.Offline(java.io.IOException("offline")))

        val result = repository.refresh()

        assertTrue(result.exceptionOrNull() is NetworkException.Offline)
        assertEquals(listOf("local"), repository.observeNotes().first().map(Note::title))
    }
}
