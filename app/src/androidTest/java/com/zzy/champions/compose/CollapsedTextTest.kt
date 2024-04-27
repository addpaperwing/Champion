package com.zzy.champions.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zzy.champions.ui.detail.compose.header.CollapsedText
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class CollapsedTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val lore = "line1\nline2\nline3\nline4"

    @Before
    fun setupCollapsedText() {
        composeTestRule.setContent {
            MyApplicationTheme {
                CollapsedText(
                    modifier = Modifier.semantics { testTag = "collapsedText" }, text = lore,
                )
            }
        }
    }

    @Test
    fun hideTextWhenThereAreMoreThan3Lines() {
        composeTestRule.onNodeWithText("line1").isDisplayed()
        composeTestRule.onNodeWithText("line4").isDisplayed().not()
    }

    @Test
    fun clickToShowDialogWhichCanDisplayAllText() {
        composeTestRule.onNodeWithTag("collapsedText").performClick()
        composeTestRule.onNodeWithText(lore).isDisplayed()
    }
}