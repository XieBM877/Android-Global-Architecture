package com.example.globalarch.feature.home

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
fun HomeRoute(
    onOpenNote: (Long) -> Unit,
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    selectedNoteId: Long? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is HomeEffect.NavigateToNote -> {
                onOpenNote(effect.id)
            }

            HomeEffect.NavigateToNewNote -> {
                onAddNote()
            }

            HomeEffect.NavigateToSettings -> {
                onOpenSettings()
            }

            is HomeEffect.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    HomeScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::dispatch,
        modifier = modifier,
        selectedNoteId = selectedNoteId,
    )
}
