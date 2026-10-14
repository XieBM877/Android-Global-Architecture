package com.example.globalarch.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<S : UiState, in I : UiIntent, E : UiEffect> : ViewModel() {
    abstract val state: StateFlow<S>

    private val effectChannel = Channel<E>(Channel.BUFFERED)

    val effects: Flow<E> = effectChannel.receiveAsFlow()

    fun dispatch(intent: I) {
        handle(intent)
    }

    protected abstract fun handle(intent: I)

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
