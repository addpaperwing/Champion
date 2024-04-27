package com.zzy.champions.index

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.local.ChampionDataPreviewParameterProvider
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.captureForPhone
import com.zzy.champions.ui.index.compose.ChampionIndexScreen
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChampionIndexScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val championData = ChampionDataPreviewParameterProvider().values.first()

    @Test
    fun championIndexOnboardingLandingScreen() {
        composeTestRule.captureForPhone("ChampionIndexOnboardingLandingScreen") {
            MyApplicationTheme {
                Scaffold { padding ->
                    ChampionIndexScreen(
                        modifier = Modifier.padding(padding),
                        championsState = UiState.Loading,
                        onboardingShowLandingScreen = true,
                        onUpdateSearchKeyword = {

                        },
                        onInsertBuilds = { },
                        onSettingClick = { },
                        onItemClick = {

                        })
                }
            }
        }
    }

    @Test
    fun championIndexScreen() {
        composeTestRule.captureForPhone("ChampionIndexScreen") {
            MyApplicationTheme {
                Scaffold { padding ->
                    ChampionIndexScreen(
                        modifier = Modifier.padding(padding),
                        championsState = UiState.Success(championData),
                        onboardingShowLandingScreen = false,
                        onUpdateSearchKeyword = {

                        },
                        onInsertBuilds = {  },
                        onSettingClick = {  },
                        onItemClick = {
                        })
                }
            }
        }

//        composeTestRule.onRoot().captureRoboImage(
//            filePath = "build/outputs/roborazzi/ChampionIndexScreen.png"
//        )
    }
}