package com.example.globalarch.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.example.globalarch.R
import com.example.globalarch.core.designsystem.component.AppTopBar
import com.example.globalarch.core.designsystem.component.constrainContentWidth
import com.example.globalarch.core.designsystem.theme.AppDimens
import com.example.globalarch.core.designsystem.theme.AppTheme
import com.example.globalarch.core.ui.component.EmptyState
import com.example.globalarch.core.ui.component.LoadingIndicator
import com.example.globalarch.core.ui.rememberSyncedTextFieldState

internal object DetailTags {
    const val Title = "detail:title"
    const val Content = "detail:content"
    const val Save = "detail:save"
    const val Delete = "detail:delete"
    const val NotFound = "detail:not_found"
}

@Composable
internal fun DetailScreen(
    state: DetailState,
    snackbarHostState: SnackbarHostState,
    onIntent: (DetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DetailTopBar(state = state, onIntent = onIntent, onNavigateBack = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val contentModifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .imePadding()
        when {
            state.isLoading -> {
                LoadingIndicator(modifier = contentModifier)
            }

            state.isNotFound -> {
                EmptyState(
                    title = stringResource(R.string.detail_error_not_found),
                    modifier = contentModifier.testTag(DetailTags.NotFound),
                )
            }

            else -> {
                DetailForm(state = state, onIntent = onIntent, modifier = contentModifier)
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val titleRes = if (state.isNew) R.string.detail_title_new else R.string.detail_title_edit
    AppTopBar(
        title = stringResource(titleRes),
        onNavigateBack = onNavigateBack,
        actions = {
            if (!state.isNew && !state.isNotFound) {
                IconButton(
                    onClick = { onIntent(DetailIntent.Delete) },
                    modifier = Modifier.testTag(DetailTags.Delete),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.detail_action_delete),
                    )
                }
            }
            if (!state.isNotFound) {
                IconButton(
                    onClick = { onIntent(DetailIntent.Save) },
                    enabled = !state.isSaving && !state.isLoading,
                    modifier = Modifier.testTag(DetailTags.Save),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.detail_action_save),
                    )
                }
            }
        },
    )
}

@Composable
private fun DetailForm(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleField = rememberSyncedTextFieldState(
        value = state.title,
        onValueChange = { onIntent(DetailIntent.TitleChanged(it)) },
    )
    val contentField = rememberSyncedTextFieldState(
        value = state.content,
        onValueChange = { onIntent(DetailIntent.ContentChanged(it)) },
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .constrainContentWidth()
            .padding(AppDimens.SpaceM),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpaceM),
    ) {
        OutlinedTextField(
            state = titleField,
            label = { Text(stringResource(R.string.detail_label_title)) },
            isError = state.titleError != null,
            supportingText = state.titleError?.let { error -> { Text(error.asString()) } },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(DetailTags.Title),
        )
        OutlinedTextField(
            state = contentField,
            label = { Text(stringResource(R.string.detail_label_content)) },
            isError = state.contentError != null,
            supportingText = state.contentError?.let { error -> { Text(error.asString()) } },
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = CONTENT_MIN_LINES),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(DetailTags.Content),
        )
    }
}

private const val CONTENT_MIN_LINES = 6

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    AppTheme {
        DetailScreen(
            state = DetailState(title = "Groceries", content = "Milk\nEggs", isNew = false),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
