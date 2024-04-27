package com.zzy.champions.compose

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.ui.detail.compose.build.BuildItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChampionBuildDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setupBuildItem() {
        composeTestRule.setContent {
            BuildItem(
                build = ChampionBuild("", ""),
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
    @Test
    fun testBuildDialogShow_whenClickEdit() {
        composeTestRule
            .onNodeWithContentDescription("Build item menu")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Champion build menu dialog")
            .isDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Edit champion build")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription( "Champion build dialog")
            .isDisplayed()

        composeTestRule
            .onNodeWithContentDescription("LDialog negative button")
            .performClick()
    }

    @Test
    fun testDeleteTextDialogShow_whenClickDelete() {
        composeTestRule
            .onNodeWithContentDescription("Build item menu")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Champion build menu dialog")
            .isDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Delete champion build")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription( "Champion build text dialog")
            .isDisplayed()

        composeTestRule
            .onNodeWithContentDescription("LDialog positive button")
            .performClick()
    }
}