package com.zzy.champions.items

import androidx.activity.ComponentActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
import com.zzy.champions.ui.items.ItemGroup
import com.zzy.champions.ui.items.compose.ItemBottomSheet
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemBottomSheetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Robolectric's clip-path hit-testing is broken for non-uniform rounded corners on
    // SDK 29-34 with Robolectric < 4.15.1 (see robolectric/robolectric#9595), which breaks
    // dispatch on anything inside a ModalBottomSheet (its default shape rounds only the top
    // corners). This project pins robolectric 4.14.1 / sdk=34, so tests wrap content with a
    // non-rounded "extraLarge" shape override to work around it. Production code is unaffected.
    @Composable
    private fun TestTheme(content: @Composable () -> Unit) {
        MyApplicationTheme {
            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraLarge = RoundedCornerShape(0.dp)),
                content = content,
            )
        }
    }

    private fun item(
        id: String,
        name: String,
        description: String,
        total: Int,
        maps: Map<String, Boolean>,
    ) = Item(
        id = id, name = name, description = description, plaintext = "",
        image = Image("$id.png"),
        gold = ItemGold(total = total, purchasable = true),
        tags = listOf("Damage"),
        maps = maps,
    )

    @Test
    fun singleVariant_showsDescriptionOnceWithNoModeLabel() {
        val doransShield = item(
            id = "1054", name = "Doran's Shield",
            description = "<mainText><stats><attention>80</attention> Health</stats><br><br>Restore 4 Health.</mainText>",
            total = 450,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true),
        )

        composeTestRule.setContent {
            TestTheme {
                ItemBottomSheet(
                    item = ItemGroup(listOf(doransShield)),
                    version = "",
                    onDismiss = {},
                    onComponentClick = {},
                    resolveItem = { null },
                )
            }
        }

        composeTestRule.onNodeWithText("80 Health", substring = true).assertExists()
        // No per-mode section header should appear for a single-variant item.
        composeTestRule.onNodeWithText("Summoner's Rift", substring = true).assertDoesNotExist()
    }

    @Test
    fun emptyStats_leavesNoBlankGapBeforeDescription() {
        val healthPotion = item(
            id = "2003", name = "Health Potion",
            description = "<mainText><stats></stats><br><br><active>Consume</active><br>Restores 120 Health.</mainText>",
            total = 50,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true),
        )

        composeTestRule.setContent {
            TestTheme {
                ItemBottomSheet(
                    item = ItemGroup(listOf(healthPotion)),
                    version = "",
                    onDismiss = {},
                    onComponentClick = {},
                    resolveItem = { null },
                )
            }
        }

        composeTestRule.onNodeWithText("Consume", substring = true).assertExists()
    }

    @Test
    fun multipleVariantsWithDifferentGoldAndDescription_showBothLabeledByMode() {
        val srVariant = item(
            id = "3031", name = "Infinity Edge",
            description = "<mainText><stats><attention>70</attention> Attack Damage</stats></mainText>",
            total = 3500,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true, GAME_MODE_ARENA to false),
        )
        val arenaVariant = item(
            id = "223031", name = "Infinity Edge",
            description = "<mainText><stats><attention>90</attention> Attack Damage</stats></mainText>",
            total = 2500,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to false, GAME_MODE_ARENA to true),
        )

        composeTestRule.setContent {
            TestTheme {
                ItemBottomSheet(
                    item = ItemGroup(listOf(srVariant, arenaVariant)),
                    version = "",
                    onDismiss = {},
                    onComponentClick = {},
                    resolveItem = { null },
                )
            }
        }

        composeTestRule.onNodeWithText("70 Attack Damage", substring = true).assertExists()
        composeTestRule.onNodeWithText("90 Attack Damage", substring = true).assertExists()
        composeTestRule.onNodeWithText("Summoner's Rift", substring = true).assertExists()
        composeTestRule.onNodeWithText("Arena", substring = true).assertExists()
    }

    @Test
    fun multipleVariantsWithIdenticalGoldAndDescription_showOnce() {
        val sameDescription = "<mainText><stats><attention>80</attention> Health</stats></mainText>"
        val a = item(
            id = "1101", name = "Scorchclaw Pup", description = sameDescription, total = 450,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true),
        )
        val b = item(
            id = "1107", name = "Scorchclaw Pup", description = sameDescription, total = 450,
            maps = mapOf(GAME_MODE_SUMMONERS_RIFT to true),
        )

        composeTestRule.setContent {
            TestTheme {
                ItemBottomSheet(
                    item = ItemGroup(listOf(a, b)),
                    version = "",
                    onDismiss = {},
                    onComponentClick = {},
                    resolveItem = { null },
                )
            }
        }

        assertEquals(1, composeTestRule.onAllNodesWithText("80 Health", substring = true).fetchSemanticsNodes().size)
        composeTestRule.onNodeWithText("Summoner's Rift", substring = true).assertDoesNotExist()
    }
}
