package com.example.globalarch.core.mvi

import app.cash.turbine.test
import com.example.globalarch.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private data class CounterState(
    val count: Int = 0,
) : UiState

private sealed interface CounterIntent : UiIntent {
    data object Increment : CounterIntent

    data class Add(
        val value: Int,
    ) : CounterIntent

    data object Announce : CounterIntent
}

private sealed interface CounterEffect : UiEffect {
    data class Announced(
        val count: Int,
    ) : CounterEffect
}

private class CounterViewModel : SelfOwnedMviViewModel<CounterState, CounterIntent, CounterEffect>(CounterState()) {
    override fun handle(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> reduce { it.copy(count = it.count + 1) }
            is CounterIntent.Add -> reduce { it.copy(count = it.count + intent.value) }
            CounterIntent.Announce -> sendEffect(CounterEffect.Announced(currentState.count))
        }
    }
}

class SelfOwnedMviViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun dispatch_increment_twice_stateCountIsTwo() {
        val viewModel = CounterViewModel()

        viewModel.dispatch(CounterIntent.Increment)
        viewModel.dispatch(CounterIntent.Increment)

        assertEquals(2, viewModel.state.value.count)
    }

    @Test
    fun dispatch_add_reducesFromPreviousState() {
        val viewModel = CounterViewModel()
        viewModel.dispatch(CounterIntent.Add(5))

        viewModel.dispatch(CounterIntent.Add(3))

        assertEquals(8, viewModel.state.value.count)
    }

    @Test
    fun dispatch_announce_deliversEffectExactlyOnce() = runTest {
        val viewModel = CounterViewModel()
        viewModel.dispatch(CounterIntent.Add(7))

        viewModel.dispatch(CounterIntent.Announce)

        viewModel.effects.test {
            assertEquals(CounterEffect.Announced(7), awaitItem())
            expectNoEvents()
        }
        viewModel.effects.test {
            expectNoEvents()
        }
    }

    @Test
    fun dispatch_announceBeforeCollection_effectIsBufferedUntilCollected() = runTest {
        val viewModel = CounterViewModel()

        viewModel.dispatch(CounterIntent.Announce)
        viewModel.dispatch(CounterIntent.Increment)
        viewModel.dispatch(CounterIntent.Announce)

        viewModel.effects.test {
            assertEquals(CounterEffect.Announced(0), awaitItem())
            assertEquals(CounterEffect.Announced(1), awaitItem())
        }
    }
}
