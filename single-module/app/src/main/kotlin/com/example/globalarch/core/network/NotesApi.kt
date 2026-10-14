package com.example.globalarch.core.network

import com.example.globalarch.core.model.Note
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

internal interface NotesApi {
    @GET("notes")
    suspend fun getNotes(): List<NoteDto>
}

@JsonClass(generateAdapter = true)
internal data class NoteDto(
    val id: Long,
    val title: String,
    val content: String,
    @Json(name = "updated_at") val updatedAtMillis: Long,
)

internal fun NoteDto.asModel(): Note = Note(
    id = id,
    title = title,
    content = content,
    updatedAtMillis = updatedAtMillis,
)
