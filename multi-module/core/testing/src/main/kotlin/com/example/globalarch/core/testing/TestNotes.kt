package com.example.globalarch.core.testing

import com.example.globalarch.core.model.Note

object TestNotes {
    val Groceries = Note(id = 1, title = "Groceries", content = "Milk\nEggs", updatedAtMillis = 1_000)
    val Ideas = Note(id = 2, title = "Ideas", content = "Build a scaffold", updatedAtMillis = 2_000)
    val All = listOf(Groceries, Ideas)
}
