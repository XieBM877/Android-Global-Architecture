package com.example.globalarch

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val UI_TIMEOUT_MILLIS = 10_000L

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun string(id: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun pressBack() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(UI_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(UI_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun createOpenAndDeleteNote_flowsThroughRealStorage() {
        val title = "E2E note ${System.currentTimeMillis()}"
        waitForTag("home:add")

        composeRule.onNodeWithTag("home:add").performClick()
        composeRule.onNodeWithTag("detail:title").performTextInput(title)
        composeRule.onNodeWithTag("detail:content").performTextInput("persisted body")
        composeRule.onNodeWithTag("detail:save").performClick()

        waitForText(title)
        composeRule.onNodeWithText(title).performClick()
        waitForTag("detail:title")
        composeRule.onNodeWithTag("detail:title").assertTextContains(title)
        pressBack()

        waitForText(title)
        composeRule
            .onNode(
                hasContentDescription(string(R.string.home_action_delete)) and hasAnyAncestor(hasText(title)),
            ).performClick()
        composeRule.waitUntil(UI_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun settings_selectingThemeUpdatesSelectionAndReturnsHome() {
        waitForTag("home:settings")

        composeRule.onNodeWithTag("home:settings").performClick()
        waitForTag("settings:theme:DARK")
        composeRule.onNodeWithTag("settings:theme:DARK").performClick()
        composeRule.onNodeWithTag("settings:theme:DARK").assertIsSelected()
        composeRule.onNodeWithTag("settings:theme:SYSTEM").performClick()
        composeRule.onNodeWithTag("settings:theme:SYSTEM").assertIsSelected()
        pressBack()

        waitForTag("home:add")
    }
}
