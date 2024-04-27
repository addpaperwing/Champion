package com.zzy.champions.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import com.zzy.champions.ui.compose.LaunchScreen
import com.zzy.champions.ui.compose.getLaunchScreenSplashWaitTime
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class LandingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Test
    fun landingScreen_infoTextVisible_whenLoadingTimeIsMoreThanSetWaitTime() {
        composeTestRule.setContent {
            MyApplicationTheme {
                LaunchScreen {
                    //nothing to do in this test
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1)
        composeTestRule.onNode(hasText("Loading")).isNotDisplayed()
        composeTestRule.mainClock.advanceTimeBy(getLaunchScreenSplashWaitTime())
        composeTestRule.onNode(hasText("Loading")).isDisplayed()
    }
}