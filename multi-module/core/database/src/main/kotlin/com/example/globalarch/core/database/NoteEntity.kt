package com.example.globalarch.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.globalarch.core.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    @ColumnInfo(name = "updated_at_millis") val updatedAtMillis: Long,
)

fun NoteEntity.asModel(): Note = Note(
    id = id,
    title = title,
    content = content,
    updatedAtMillis = updatedAtMillis,
)

fun Note.asEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    updatedAtMillis = updatedAtMillis,
)
