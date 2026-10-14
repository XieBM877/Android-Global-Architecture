package com.example.globalarch.core.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.globalarch.R
import com.example.globalarch.core.designsystem.theme.AppTheme
import com.example.globalarch.core.ui.component.ErrorState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val retryLabel: String
        get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.ui_action_retry)

    @Test
    fun errorState_showsMessageAndRetryButton() {
        composeRule.setContent {
            AppTheme { ErrorState(message = "Something broke", onRetry = {}) }
        }

        composeRule.onNodeWithText("Something broke").assertIsDisplayed()
        composeRule.onNodeWithText(retryLabel).assertIsDisplayed()
    }

    @Test
    fun errorState_retryClick_invokesCallbackOnce() {
        var retryCount = 0
        composeRule.setContent {
            AppTheme { ErrorState(message = "Something broke", onRetry = { retryCount++ }) }
        }

        composeRule.onNodeWithText(retryLabel).performClick()

        assertEquals(1, retryCount)
    }
}
