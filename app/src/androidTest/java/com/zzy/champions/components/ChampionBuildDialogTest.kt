package com.zzy.champions.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.ui.detail.compose.cb.BuildItem
import org.junit.Rule
import org.junit.Test

class ChampionBuildDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testBuildItemDialogs() {
        composeTestRule.setContent {
            BuildItem(
                cb = ChampionBuild("", ""),
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }

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


        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

//        composeTestRule
//            .onNodeWithContentDescription( "Champion build dialog")
//            .isNotDisplayed()

        composeTestRule
            .onNodeWithContentDescription( "Delete champion build")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription( "Champion build text dialog")
            .isDisplayed()

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

//        composeTestRule
//            .onNodeWithContentDescription( "Champion build text dialog")
//            .isNotDisplayed()
    }
}