package com.example.globalarch.core.network

import com.example.globalarch.core.model.Note

interface NotesRemoteDataSource {
    suspend fun fetchNotes(): Result<List<Note>>
}

internal class RetrofitNotesRemoteDataSource(
    private val api: NotesApi,
) : NotesRemoteDataSource {
    override suspend fun fetchNotes(): Result<List<Note>> = safeApiCall {
        api.getNotes().map(NoteDto::asModel)
    }
}

internal object NoOpNotesRemoteDataSource : NotesRemoteDataSource {
    override suspend fun fetchNotes(): Result<List<Note>> = Result.success(emptyList())
}
