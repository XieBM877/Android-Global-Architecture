package com.example.globalarch.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.globalarch.core.designsystem.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<HomeIntent>()

    private val sampleNotes = listOf(
        NoteItem(id = 1, title = "Groceries", preview = "Milk"),
        NoteItem(id = 2, title = "Ideas", preview = "Scaffold"),
    )

    private fun show(
        state: HomeState,
        selectedNoteId: Long? = null,
    ) {
        composeRule.setContent {
            AppTheme {
                HomeScreen(
                    state = state,
                    snackbarHostState = remember { SnackbarHostState() },
                    onIntent = { intents += it },
                    selectedNoteId = selectedNoteId,
                )
            }
        }
    }

    @Test
    fun loadingState_hidesListAndEmptyMessage() {
        show(HomeState(isLoading = true))

        composeRule.onNodeWithTag(HomeTags.List).assertDoesNotExist()
        composeRule.onNodeWithTag(HomeTags.Empty).assertDoesNotExist()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        show(HomeState(isLoading = false))

        composeRule.onNodeWithTag(HomeTags.Empty).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.List).assertDoesNotExist()
    }

    @Test
    fun notes_areListedWithTitles() {
        show(HomeState(notes = sampleNotes, isLoading = false))

        composeRule.onNodeWithTag(HomeTags.note(1)).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTags.note(2)).assertIsDisplayed()
    }

    @Test
    fun rowClick_dispatchesOpenNote() {
        show(HomeState(notes = sampleNotes, isLoading = false))

        composeRule.onNodeWithTag(HomeTags.note(2)).performClick()

        assertEquals(listOf<HomeIntent>(HomeIntent.OpenNote(2)), intents)
    }

    @Test
    fun deleteClick_dispatchesDeleteNoteWithoutOpeningIt() {
        show(HomeState(notes = sampleNotes, isLoading = false))

        composeRule.onNodeWithTag(HomeTags.delete(1)).performClick()

        assertEquals(listOf<HomeIntent>(HomeIntent.DeleteNote(1)), intents)
    }

    @Test
    fun fabClick_dispatchesAddNote() {
        show(HomeState(isLoading = false))

        composeRule.onNodeWithTag(HomeTags.Add).performClick()

        assertEquals(listOf<HomeIntent>(HomeIntent.AddNote), intents)
    }

    @Test
    fun settingsClick_dispatchesOpenSettings() {
        show(HomeState(isLoading = false))

        composeRule.onNodeWithTag(HomeTags.Settings).performClick()

        assertEquals(listOf<HomeIntent>(HomeIntent.OpenSettings), intents)
    }
}
