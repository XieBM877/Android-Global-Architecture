package com.example.globalarch.feature.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.globalarch.R
import com.example.globalarch.core.designsystem.theme.AppTheme
import com.example.globalarch.core.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<DetailIntent>()
    private var backCount = 0

    private fun show(state: DetailState) {
        composeRule.setContent {
            AppTheme {
                DetailScreen(
                    state = state,
                    snackbarHostState = remember { SnackbarHostState() },
                    onIntent = { intents += it },
                    onNavigateBack = { backCount++ },
                )
            }
        }
    }

    @Test
    fun newNote_hidesDeleteAction() {
        show(DetailState(isNew = true))

        composeRule.onNodeWithTag(DetailTags.Title).assertIsDisplayed()
        composeRule.onNodeWithTag(DetailTags.Content).assertIsDisplayed()
        composeRule.onNodeWithTag(DetailTags.Delete).assertDoesNotExist()
    }

    @Test
    fun existingNote_showsStoredTextAndDeleteAction() {
        show(DetailState(title = "Groceries", content = "Milk", isNew = false))

        composeRule.onNodeWithTag(DetailTags.Title).assertTextContains("Groceries")
        composeRule.onNodeWithTag(DetailTags.Content).assertTextContains("Milk")
        composeRule.onNodeWithTag(DetailTags.Delete).assertIsDisplayed()
    }

    @Test
    fun typingInTitle_dispatchesTitleChanged() {
        show(DetailState())

        composeRule.onNodeWithTag(DetailTags.Title).performTextInput("Plan")
        composeRule.waitForIdle()

        assertEquals(listOf<DetailIntent>(DetailIntent.TitleChanged("Plan")), intents)
    }

    @Test
    fun saveClick_dispatchesSave() {
        show(DetailState(title = "Plan"))

        composeRule.onNodeWithTag(DetailTags.Save).performClick()

        assertEquals(listOf<DetailIntent>(DetailIntent.Save), intents)
    }

    @Test
    fun deleteClick_dispatchesDelete() {
        show(DetailState(title = "Plan", isNew = false))

        composeRule.onNodeWithTag(DetailTags.Delete).performClick()

        assertEquals(listOf<DetailIntent>(DetailIntent.Delete), intents)
    }

    @Test
    fun saving_disablesSaveAction() {
        show(DetailState(title = "Plan", isSaving = true))

        composeRule.onNodeWithTag(DetailTags.Save).assertIsNotEnabled()
    }

    @Test
    fun titleError_isDisplayedNextToField() {
        show(DetailState(titleError = UiText.Plain("Title can't be empty")))

        composeRule.onNodeWithText("Title can't be empty").assertIsDisplayed()
    }

    @Test
    fun backClick_invokesNavigateBackOnce() {
        show(DetailState())
        val backLabel = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(R.string.designsystem_action_back)

        composeRule.onNodeWithContentDescription(backLabel).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun notFound_showsMessageAndNoForm() {
        show(DetailState(isNotFound = true, isNew = false))

        composeRule.onNodeWithTag(DetailTags.NotFound).assertIsDisplayed()
        composeRule.onNodeWithTag(DetailTags.Title).assertDoesNotExist()
        composeRule.onNodeWithTag(DetailTags.Save).assertDoesNotExist()
    }
}
