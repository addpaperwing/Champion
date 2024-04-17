package com.zzy.champions.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.local.ChampionAndDetailPreviewParameterProvider
import com.zzy.champions.data.local.ChampionBuildsPreviewParameterProvider
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.DefaultTestDevices
import com.zzy.champions.ui.capture
import com.zzy.champions.ui.captureForPhone
import com.zzy.champions.ui.detail.compose.ChampionDetailScreen
import com.zzy.champions.ui.setContentForDevice
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChampionDetailScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val championAndDetail = ChampionAndDetailPreviewParameterProvider().values.first()
    private val championBuilds = ChampionBuildsPreviewParameterProvider().values.first()

    @Test
    fun championDetailScreenLoading() {
        composeTestRule.captureForPhone("ChampionDetailScreenLoading") {
            MyApplicationTheme {
                Scaffold { padding ->
                    ChampionDetailScreen(
                        modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                        championDetailState = UiState.Loading,
                        getChampionDetail = { },
                        onSaveBannerNumber = { _, _ -> },
                        championBuilds = emptyList(),
                        getChampionBuilds = { },
                        addNewBuild = {},
                        onOpenBrowser = {},
                        updateBuild = {},
                        deleteBuild = {}
                    )
                }
            }
        }
    }

    @Test
    fun championDetailScreenError() {
        composeTestRule.captureForPhone("ChampionDetailScreenError") {
            MyApplicationTheme {
                Scaffold { padding ->
                    ChampionDetailScreen(
                        modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                        championDetailState = UiState.Error(IOException()),
                        getChampionDetail = { },
                        onSaveBannerNumber = { _, _ -> },
                        championBuilds = emptyList(),
                        getChampionBuilds = { },
                        addNewBuild = {},
                        onOpenBrowser = {},
                        updateBuild = {},
                        deleteBuild = {}
                    )
                }
            }
        }
    }

    @Test
    fun championDetailScreenSuccess() {
        val device = DefaultTestDevices.PHONE
        composeTestRule.setContentForDevice(device) {
            MyApplicationTheme {
                Scaffold { padding ->
                    ChampionDetailScreen(
                        modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                        championDetailState = UiState.Success(championAndDetail),
                        getChampionDetail = { },
                        onSaveBannerNumber = { _, _ -> },
                        championBuilds = championBuilds,
                        getChampionBuilds = { },
                        addNewBuild = {},
                        onOpenBrowser = {},
                        updateBuild = {},
                        deleteBuild = {}
                    )
                }
            }
        }

        composeTestRule.capture(device.description, "ChampionDetailScreen_abilities")

        composeTestRule.onNodeWithContentDescription("Stats tab").performClick()
        composeTestRule.capture(device.description, "ChampionDetailScreen_stats")

        composeTestRule.onNodeWithContentDescription("Builds tab").performClick()
        composeTestRule.capture(device.description, "ChampionDetailScreen_builds")

        composeTestRule.onNodeWithContentDescription("Skins tab").performClick()
        composeTestRule.capture(device.description, "ChampionDetailScreen_skins")
    }
}