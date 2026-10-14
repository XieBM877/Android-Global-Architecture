package com.example.globalarch.core.model

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val updatedAtMillis: Long,
) {
    companion object {
        const val NEW_ID = 0L
    }
}
