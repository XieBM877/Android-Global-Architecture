package com.example.globalarch.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.globalarch.core.designsystem.component.AppTopBar
import com.example.globalarch.core.designsystem.theme.AppDimens
import com.example.globalarch.core.designsystem.theme.AppTheme
import com.example.globalarch.core.ui.component.EmptyState
import com.example.globalarch.core.ui.component.LoadingIndicator

internal object HomeTags {
    const val Add = "home:add"
    const val Settings = "home:settings"
    const val List = "home:list"
    const val Empty = "home:empty"

    fun note(id: Long) = "home:note:$id"

    fun delete(id: Long) = "home:delete:$id"
}

@Composable
internal fun HomeScreen(
    state: HomeState,
    snackbarHostState: SnackbarHostState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    selectedNoteId: Long? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
                actions = {
                    IconButton(
                        onClick = { onIntent(HomeIntent.OpenSettings) },
                        modifier = Modifier.testTag(HomeTags.Settings),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.home_action_settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(HomeIntent.AddNote) },
                modifier = Modifier.testTag(HomeTags.Add),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.home_action_add),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        HomeContent(
            state = state,
            onIntent = onIntent,
            selectedNoteId = selectedNoteId,
            modifier = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    selectedNoteId: Long?,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onIntent(HomeIntent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            state.isLoading -> {
                LoadingIndicator()
            }

            state.notes.isEmpty() -> {
                EmptyState(
                    title = stringResource(R.string.home_empty_title),
                    message = stringResource(R.string.home_empty_message),
                    modifier = Modifier
                        .testTag(HomeTags.Empty)
                        .verticalScroll(rememberScrollState()),
                )
            }

            else -> {
                NoteList(
                    notes = state.notes,
                    selectedNoteId = selectedNoteId,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun NoteList(
    notes: List<NoteItem>,
    selectedNoteId: Long?,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(HomeTags.List),
        contentPadding = PaddingValues(bottom = AppDimens.FabClearance),
    ) {
        items(items = notes, key = NoteItem::id) { note ->
            NoteRow(
                note = note,
                selected = note.id == selectedNoteId,
                onOpen = { onIntent(HomeIntent.OpenNote(note.id)) },
                onDelete = { onIntent(HomeIntent.DeleteNote(note.id)) },
            )
        }
    }
}

@Composable
private fun NoteRow(
    note: NoteItem,
    selected: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    ListItem(
        headlineContent = { Text(text = note.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = if (note.preview.isEmpty()) {
            null
        } else {
            { Text(text = note.preview, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        },
        trailingContent = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag(HomeTags.delete(note.id)),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.home_action_delete),
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier = modifier
            .clickable(onClick = onOpen)
            .testTag(HomeTags.note(note.id)),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            state = HomeState(
                notes = listOf(
                    NoteItem(1, "Groceries", "Milk and eggs"),
                    NoteItem(2, "Ideas", "Build a scaffold"),
                ),
                isLoading = false,
            ),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            selectedNoteId = 2,
        )
    }
}
