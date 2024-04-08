package com.zzy.champions.components

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.zzy.champions.ui.detail.compose.LoadingAndErrorScreen
import org.junit.Rule
import org.junit.Test

class LoadingAndErrorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun loadingDisplayed() {
        composeTestRule.setContent {
            LoadingAndErrorScreen(
                isLoading = true,
                isError = false) {

            }
        }

        composeTestRule
            .onNodeWithContentDescription("Loading indicator")
            .isDisplayed()
    }
}