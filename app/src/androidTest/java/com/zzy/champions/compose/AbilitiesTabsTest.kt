package com.zzy.champions.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.Spell
import com.zzy.champions.ui.detail.compose.ability.Abilities
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AbilitiesTabsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setupAbilitiesTabs() {
        composeTestRule.setContent {
            Abilities(version = "", abilities = listOf(
                Passive("p", "p", Image("")),
                Spell("q", "q", Image("")),
                Spell("w", "w", Image("")),
                Spell("e", "e", Image("")),
                Spell("r", "r", Image("")),
            ))
        }
    }

    @Test
    fun abilitiesTabs_defaultShowPassive() {
        composeTestRule
            .onNodeWithContentDescription("ability content 0")
            .assertIsDisplayed()
    }

    @Test
    fun abilitiesTabs_clickQ_showQ() {
        composeTestRule
            .onNodeWithContentDescription("ability icon 1")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("ability content 1")
            .assertIsDisplayed()
    }

    @Test
    fun abilitiesTabs_clickW_showW() {
        composeTestRule
            .onNodeWithContentDescription("ability icon 2")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("ability content 2")
            .assertIsDisplayed()
    }

    @Test
    fun abilitiesTabs_clickE_showE() {
        composeTestRule
            .onNodeWithContentDescription("ability icon 3")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("ability content 3")
            .assertIsDisplayed()
    }

    @Test
    fun abilitiesTabs_clickR_showR() {
        composeTestRule
            .onNodeWithContentDescription("ability icon 4")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("ability content 4")
            .assertIsDisplayed()
    }

    @Test
    fun abilitiesTabs_clickP_showP() {
        composeTestRule
            .onNodeWithContentDescription("ability icon 0")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("ability content 0")
            .assertIsDisplayed()
    }
}