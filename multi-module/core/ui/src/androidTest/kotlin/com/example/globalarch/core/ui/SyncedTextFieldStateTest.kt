package com.example.globalarch.core.ui

import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.globalarch.core.designsystem.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val FIELD_TAG = "synced:field"

@RunWith(AndroidJUnit4::class)
class SyncedTextFieldStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val reported = mutableListOf<String>()

    private fun show(initial: String) {
        composeRule.setContent {
            AppTheme {
                OutlinedTextField(
                    state = rememberSyncedTextFieldState(value = initial, onValueChange = { reported += it }),
                    modifier = Modifier.testTag(FIELD_TAG),
                )
            }
        }
    }

    @Test
    fun initialValue_isShownWithoutReporting() {
        show("Groceries")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(FIELD_TAG).assertTextContains("Groceries")
        assertTrue(reported.isEmpty())
    }

    @Test
    fun typing_reportsFullTextToCaller() {
        show("")

        composeRule.onNodeWithTag(FIELD_TAG).performTextInput("Plan")
        composeRule.waitForIdle()

        assertEquals(listOf("Plan"), reported)
    }
}
