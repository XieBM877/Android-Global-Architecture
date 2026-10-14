package com.example.globalarch.core.network

import com.example.globalarch.core.model.Note
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetrofitNotesRemoteDataSourceTest {
    private val server = MockWebServer()
    private lateinit var dataSource: NotesRemoteDataSource

    @Before
    fun setUp() {
        server.start()
        val retrofit =
            createRetrofit(server.url("/").toString(), createOkHttpClient(enableLogging = false), createMoshi())
        dataSource = RetrofitNotesRemoteDataSource(retrofit.create(NotesApi::class.java))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun fetchNotes_successResponse_mapsDtoToDomainModel() = runTest {
        server.enqueue(
            MockResponse
                .Builder()
                .body("""[{"id":7,"title":"Remote","content":"Body","updated_at":1234}]""")
                .build(),
        )

        val result = dataSource.fetchNotes()

        assertEquals(
            listOf(Note(id = 7, title = "Remote", content = "Body", updatedAtMillis = 1234)),
            result.getOrThrow(),
        )
    }

    @Test
    fun fetchNotes_serverError_failsWithHttpException() = runTest {
        server.enqueue(MockResponse.Builder().code(503).build())

        val failure = dataSource.fetchNotes().exceptionOrNull()

        assertTrue(failure is NetworkException.Http)
        assertEquals(503, (failure as NetworkException.Http).code)
    }

    @Test
    fun fetchNotes_malformedJson_failsWithParseException() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"unexpected":true}""").build())

        val failure = dataSource.fetchNotes().exceptionOrNull()

        assertTrue(failure is NetworkException.Parse)
    }

    @Test
    fun fetchNotes_connectionDropped_failsWithOfflineException() = runTest {
        server.enqueue(MockResponse.Builder().onRequestStart(SocketEffect.CloseSocket()).build())

        val failure = dataSource.fetchNotes().exceptionOrNull()

        assertTrue(failure is NetworkException.Offline)
    }

    @Test
    fun noOpDataSource_fetchNotes_succeedsWithEmptyList() = runTest {
        assertEquals(emptyList<Note>(), NoOpNotesRemoteDataSource.fetchNotes().getOrThrow())
    }
}
