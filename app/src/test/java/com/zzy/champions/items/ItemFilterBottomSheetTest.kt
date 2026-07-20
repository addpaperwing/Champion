package com.zzy.champions.items

import androidx.activity.ComponentActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.compose.ItemFilterBottomSheet
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemFilterBottomSheetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Robolectric's clip-path hit-testing is broken for non-uniform rounded corners on
    // SDK 29-34 with Robolectric < 4.15.1 (see robolectric/robolectric#9595), which breaks
    // click dispatch on any clickable inside a ModalBottomSheet (its default shape rounds
    // only the top corners). This project pins robolectric 4.14.1 / sdk=34, so tests wrap
    // content with a non-rounded "extraLarge" shape override to work around it. Production
    // code is unaffected.
    @Composable
    private fun TestTheme(content: @Composable () -> Unit) {
        MyApplicationTheme {
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraLarge = RoundedCornerShape(0.dp)),
                content = content,
            )
        }
    }

    @Test
    fun tappingTagChip_invokesOnTagToggle() {
        var toggledTag: String? = null

        composeTestRule.setContent {
            TestTheme {
                ItemFilterBottomSheet(
                    availableTags = listOf("Boots", "Damage"),
                    selectedTags = emptySet(),
                    selectedGameModes = emptySet(),
                    onTagToggle = { toggledTag = it },
                    onGameModeToggle = {},
                    onClearAll = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Damage").performClick()

        assertEquals("Damage", toggledTag)
    }

    @Test
    fun tappingClearAll_invokesOnClearAll() {
        var cleared = false

        composeTestRule.setContent {
            TestTheme {
                ItemFilterBottomSheet(
                    availableTags = listOf("Boots"),
                    selectedTags = emptySet(),
                    selectedGameModes = emptySet(),
                    onTagToggle = {},
                    onGameModeToggle = {},
                    onClearAll = { cleared = true },
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clear all").performClick()

        assertTrue(cleared)
    }

    @Test
    fun tappingGameModeChip_invokesOnGameModeToggle() {
        var toggledMode: String? = null

        composeTestRule.setContent {
            TestTheme {
                ItemFilterBottomSheet(
                    availableTags = emptyList(),
                    selectedTags = emptySet(),
                    selectedGameModes = emptySet(),
                    onTagToggle = {},
                    onGameModeToggle = { toggledMode = it },
                    onClearAll = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ARAM").performClick()

        assertEquals(GAME_MODE_ARAM, toggledMode)
    }

    @Test
    fun multipleGameModeChipsCanBeSelectedSimultaneously() {
        composeTestRule.setContent {
            TestTheme {
                ItemFilterBottomSheet(
                    availableTags = emptyList(),
                    selectedTags = emptySet(),
                    selectedGameModes = setOf(GAME_MODE_ARAM, GAME_MODE_ARENA),
                    onTagToggle = {},
                    onGameModeToggle = {},
                    onClearAll = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ARAM").assertIsSelected()
        composeTestRule.onNodeWithText("Arena").assertIsSelected()
        composeTestRule.onNodeWithText("Summoner's Rift").assertIsNotSelected()
    }
}
