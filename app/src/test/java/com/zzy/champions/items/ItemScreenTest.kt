package com.zzy.champions.items

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.longSword
import com.zzy.champions.sorceresShoes
import com.zzy.champions.ui.items.ItemListDisplay
import com.zzy.champions.ui.items.compose.ItemScreen
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

// Robolectric's clip-path hit-testing is broken for shaped clickables on SDK 29-34 with
// Robolectric < 4.15.1 (see robolectric/robolectric#9595). FilterIconButton's IconButton is
// clipped to a hardcoded CircleShape, so performClick() on it silently misses the click
// (dispatched to nothing) under the default LEGACY graphics mode. NATIVE mode uses the real
// Skia pipeline and hit-tests correctly; production code is unaffected.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ItemScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun categorizedDisplay_showsCategoryHeaders() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ItemScreen(
                    itemListState = UiState.Success(
                        ItemListDisplay.Categorized(listOf("Starter" to listOf(longSword)))
                    ),
                    version = "",
                    onItemClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("STARTER").assertExists()
        composeTestRule.onNodeWithText(longSword.name).assertExists()
    }

    @Test
    fun flatDisplay_showsNoHeaders() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ItemScreen(
                    itemListState = UiState.Success(
                        ItemListDisplay.Flat(listOf(sorceresShoes))
                    ),
                    version = "",
                    onItemClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("STARTER").assertDoesNotExist()
        composeTestRule.onNodeWithText(sorceresShoes.name).assertExists()
    }

    @Test
    fun filterIconClick_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                ItemScreen(
                    itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                    version = "",
                    onFilterIconClick = { clicked = true },
                    onItemClick = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Filter items").performClick()

        assert(clicked)
    }
}
