package com.example.globalarch.core.common

import kotlin.coroutines.cancellation.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultExtensionsTest {
    @Test
    fun suspendRunCatching_blockReturns_wrapsSuccess() {
        val result = suspendRunCatching { 42 }

        assertEquals(42, result.getOrThrow())
    }

    @Test
    fun suspendRunCatching_blockThrows_wrapsFailure() {
        val failure = IllegalStateException("boom")

        val result = suspendRunCatching<Int> { throw failure }

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }

    @Test
    fun suspendRunCatching_cancellation_isRethrownInsteadOfWrapped() {
        assertThrows(CancellationException::class.java) {
            suspendRunCatching<Int> { throw CancellationException("cancelled") }
        }
    }
}
