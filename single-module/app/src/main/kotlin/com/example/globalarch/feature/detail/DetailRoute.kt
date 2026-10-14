package com.example.globalarch.feature.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.globalarch.core.ui.CollectEffects
import kotlinx.coroutines.launch

@Composable
fun NoteDetailRoute(
    noteId: Long?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(noteId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            DetailEffect.NavigateBack -> {
                onNavigateBack()
            }

            is DetailEffect.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    DetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::dispatch,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
