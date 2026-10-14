package com.example.globalarch.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java,
            ).allowMainThreadQueries()
            .build()
        dao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_newEntity_assignsGeneratedIdAndIsObservable() = runTest {
        val id = dao.upsert(NoteEntity(title = "a", content = "b", updatedAtMillis = 1))

        assertTrue(id > 0)
        assertEquals("a", dao.observeById(id).first()?.title)
    }

    @Test
    fun upsert_existingEntity_replacesRowInPlace() = runTest {
        val id = dao.upsert(NoteEntity(title = "old", content = "", updatedAtMillis = 1))

        dao.upsert(NoteEntity(id = id, title = "new", content = "", updatedAtMillis = 2))

        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("new", all.single().title)
    }

    @Test
    fun observeAll_ordersByUpdatedAtDescending() = runTest {
        dao.upsert(NoteEntity(title = "older", content = "", updatedAtMillis = 1))
        dao.upsert(NoteEntity(title = "newer", content = "", updatedAtMillis = 5))

        val titles = dao.observeAll().first().map(NoteEntity::title)

        assertEquals(listOf("newer", "older"), titles)
    }

    @Test
    fun deleteById_removesOnlyTargetRow() = runTest {
        val keep = dao.upsert(NoteEntity(title = "keep", content = "", updatedAtMillis = 1))
        val drop = dao.upsert(NoteEntity(title = "drop", content = "", updatedAtMillis = 2))

        dao.deleteById(drop)

        assertNull(dao.observeById(drop).first())
        assertEquals("keep", dao.observeById(keep).first()?.title)
    }
}
