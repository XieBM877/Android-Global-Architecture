package com.example.globalarch.core.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class SelfOwnedMviViewModel<S : UiState, in I : UiIntent, E : UiEffect>(
    initialState: S,
) : MviViewModel<S, I, E>() {
    private val mutableState = MutableStateFlow(initialState)

    final override val state: StateFlow<S> = mutableState.asStateFlow()

    protected val currentState: S
        get() = mutableState.value

    protected fun reduce(transform: (S) -> S) {
        mutableState.update(transform)
    }
}
