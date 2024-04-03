package com.zzy.champions.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.zzy.champions.AndroidTestUtil
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.ui.detail.compose.ChampionDetailTabPager
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DetailTabsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setupDetailTabs() {
        composeTestRule.setContent {
            ChampionDetailTabPager(
                champion = AndroidTestUtil.createChampion(),
                detail = AndroidTestUtil.createChampionDetail("aatrox"),
                championBuilds = listOf(ChampionBuild("", ""))
            )
        }
    }

    @Test
    fun detailTabs_defaultShowAbilities() {
        composeTestRule
            .onNodeWithContentDescription("Champion abilities")
            .assertIsDisplayed()
    }

    @Test
    fun detailTabs_clickStats_showStatsContent() {
        composeTestRule
            .onNodeWithContentDescription("Stats tab")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Champion Stats")
            .assertIsDisplayed()
    }

    @Test
    fun detailTabs_clickBuild_showBuildContent() {
        composeTestRule
            .onNodeWithContentDescription("Builds tab")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Champion build")
            .assertIsDisplayed()
    }

    @Test
    fun detailTabs_clickSkins_showSkinsContent() {
        composeTestRule
            .onNodeWithContentDescription("Skins tab")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Champion skins")
            .assertIsDisplayed()
    }

    @Test
    fun detailTabs_clickAbility_showAbilitiesContent() {
        composeTestRule
            .onNodeWithContentDescription("Abilities tab")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Champion abilities")
            .assertIsDisplayed()
    }
}