package com.zzy.champions.index

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.zzy.champions.ui.index.compose.SearchTextField
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun trailingContent_rendersAlongsideClearIcon() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SearchTextField(
                    text = "boots",
                    onTextChanged = {},
                    onClearText = {},
                    onDone = {},
                    trailingContent = { Text("FILTER_MARKER") },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
        composeTestRule.onNodeWithText("FILTER_MARKER").assertIsDisplayed()
    }

    @Test
    fun noTrailingContent_rendersOnlyClearIcon() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SearchTextField(
                    text = "boots",
                    onTextChanged = {},
                    onClearText = {},
                    onDone = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
        composeTestRule.onNodeWithText("FILTER_MARKER").assertIsNotDisplayed()
    }
}
