package com.example.globalarch.core.domain.repository

import com.example.globalarch.core.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>

    fun observeNote(id: Long): Flow<Note?>

    suspend fun upsert(note: Note): Long

    suspend fun delete(id: Long)

    suspend fun refresh(): Result<Unit>
}
