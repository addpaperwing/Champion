# Item Filter AND-Semantics + Multi-Select Game Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the Arena game-mode bug, make tag and game-mode filtering AND-within-group instead of OR-within-group, and turn game mode from single-select into multi-select — per `docs/specs/2026-07-19-item-filter-and-semantics-design.md`.

**Architecture:** All changes are confined to `ItemViewModel.kt` (state shape + filter predicate), `ItemFilterBottomSheet.kt` (multi-select chips), and `ItemScreen.kt`/`ItemRoute` (multi-chip active-filter row). No new files, no schema/network changes.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Hilt ViewModel with `SavedStateHandle`, JUnit4 + MockK + Robolectric/Compose test rules.

## Global Constraints

- Category filtering stays OR-within-group (unchanged) — do not touch `categoryByItemId[item.id] in categories`.
- Cross-group combination (category AND tags AND game modes AND search) stays AND — unchanged, already correct.
- The curated three-mode list (`ALL_GAME_MODES`) stays fixed at three entries — no dynamic/derived list.
- Every task must leave `.\gradlew.bat testDebugUnitTest` fully green before moving to the next task.

---

### Task 1: Fix the Arena map-ID bug

**Files:**
- Modify: `app/src/test/java/com/zzy/champions/TestItemRepository.kt:16,30,44`
- Modify: `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt:51`
- Test: `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt:217-229`

**Interfaces:**
- Consumes: nothing new (existing `GAME_MODE_ARENA` constant, existing `ItemRepository` fixtures).
- Produces: `GAME_MODE_ARENA` now equals `"30"` (was `"22"`) — every later task treats this as the correct value.

- [ ] **Step 1: Update the test fixtures to mirror real Data Dragon shape**

In `app/src/test/java/com/zzy/champions/TestItemRepository.kt`, replace the three `maps = ...` lines so that key `"22"` (TFT's Convergence — always false for League items, including Arena ones) is present but always false, and key `"30"` (the real Arena map ID) carries the Arena availability that `"22"` used to carry:

```kotlin
// longSword (line 16)
maps = mapOf("11" to true, "12" to false, "22" to false, "30" to false), // Summoner's Rift only
```

```kotlin
// infinityEdge (line 30)
maps = mapOf("11" to true, "12" to true, "22" to false, "30" to false), // Summoner's Rift + ARAM
```

```kotlin
// sorceresShoes (line 44)
maps = mapOf("11" to false, "12" to true, "22" to false, "30" to true), // ARAM + Arena
```

- [ ] **Step 2: Run the existing game-mode test to confirm it now fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest.gameModeFilter_showsFlatDisplayWithMatchingItems"`
Expected: FAIL. The test selects `GAME_MODE_ARENA` (still `"22"` at this point) and expects `sorceresShoes`, but `sorceresShoes.maps["22"]` is now `false` (only `"30"` is `true`), so the filtered list is empty instead of `[sorceresShoes]`. This confirms the fixture change reproduces the real-world bug.

- [ ] **Step 3: Fix the constant**

In `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt:51`, change:

```kotlin
internal const val GAME_MODE_ARENA          = "22"
```

to:

```kotlin
internal const val GAME_MODE_ARENA          = "30"
```

- [ ] **Step 4: Run the full test suite to confirm everything passes again**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: PASS (all tests, including `gameModeFilter_showsFlatDisplayWithMatchingItems`).

- [ ] **Step 5: Commit**

```bash
git add app/src/test/java/com/zzy/champions/TestItemRepository.kt app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt
git commit -m "fix: correct Arena game-mode map ID from 22 (TFT Convergence) to 30"
```

---

### Task 2: ViewModel — AND-within-group for tags, multi-select AND for game mode

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`
- Test: `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`

**Interfaces:**
- Consumes: `GAME_MODE_ARENA`/`GAME_MODE_ARAM`/`GAME_MODE_SUMMONERS_RIFT` = `"30"`/`"12"`/`"11"` (Task 1); `TestItemRepository` fixtures from Task 1.
- Produces: `val selectedGameModes: StateFlow<Set<String>>` (replaces `selectedGameMode: StateFlow<String?>`); `fun toggleGameMode(mapId: String)` (replaces `fun selectGameMode(mapId: String)`); tag predicate is now AND (`tags.all { it in item.tags }` instead of `.any`); game-mode predicate is now AND-across-selected-modes (`gameModes.all { item.maps[it] == true }`). Later tasks (bottom sheet, screen) consume `selectedGameModes` and `toggleGameMode` under these exact names/signatures.

- [ ] **Step 1: Write the failing tests for AND-within-tags**

In `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`, replace the `multipleTagsSelected_matchEitherTag` test (lines 123-136) with two tests:

```kotlin
@Test
fun multipleTagsSelected_matchesOnlyItemWithBothTags() = runTest {
    val job = launch { viewModel.itemListState.collect() }
    advanceUntilIdle()

    viewModel.toggleTagFilter("Damage")
    viewModel.toggleTagFilter("CriticalStrike")
    advanceUntilIdle()

    val display = (viewModel.itemListState.value as UiState.Success).data
    assertTrue(display is ItemListDisplay.Flat)
    assertEquals(listOf(infinityEdge), (display as ItemListDisplay.Flat).items)
    job.cancel()
}

@Test
fun multipleTagsSelected_excludesItemsMissingOneTag() = runTest {
    val job = launch { viewModel.itemListState.collect() }
    advanceUntilIdle()

    // sorceresShoes has "Boots" but not "CriticalStrike"; infinityEdge has "CriticalStrike"
    // but not "Boots". Neither item has both, so AND semantics must exclude both.
    viewModel.toggleTagFilter("Boots")
    viewModel.toggleTagFilter("CriticalStrike")
    advanceUntilIdle()

    val display = (viewModel.itemListState.value as UiState.Success).data
    assertTrue(display is ItemListDisplay.Flat)
    assertTrue((display as ItemListDisplay.Flat).items.isEmpty())
    job.cancel()
}
```

- [ ] **Step 2: Run the new tag tests to confirm they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest.multipleTagsSelected*"`
Expected: FAIL. `multipleTagsSelected_matchesOnlyItemWithBothTags` fails because current OR logic returns `[sorceresShoes, infinityEdge]`, not `[infinityEdge]`. `multipleTagsSelected_excludesItemsMissingOneTag` fails because current OR logic returns `[sorceresShoes, infinityEdge]`, not an empty list.

- [ ] **Step 3: Write the failing tests for multi-select game mode**

Replace `selectedGameMode_isNullInitially` (lines 212-215), `selectingSameGameModeAgain_clearsSelection` (lines 231-245), and `selectingDifferentGameMode_replacesSelection` (lines 247-262) with:

```kotlin
@Test
fun selectedGameModes_isEmptyInitially() {
    assertTrue(viewModel.selectedGameModes.value.isEmpty())
}

@Test
fun togglingSameGameModeTwice_clearsSelection() = runTest {
    val job = launch { viewModel.itemListState.collect() }
    advanceUntilIdle()

    viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
    advanceUntilIdle()
    viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
    advanceUntilIdle()

    assertTrue(viewModel.selectedGameModes.value.isEmpty())
    val display = (viewModel.itemListState.value as UiState.Success).data
    assertTrue(display is ItemListDisplay.Categorized)
    job.cancel()
}

@Test
fun togglingTwoGameModes_bothStaySelectedAndMustBothMatch() = runTest {
    val job = launch { viewModel.itemListState.collect() }
    advanceUntilIdle()

    viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
    advanceUntilIdle()
    var display = (viewModel.itemListState.value as UiState.Success).data
    assertEquals(listOf(longSword, infinityEdge), (display as ItemListDisplay.Flat).items)

    // Adding ARAM on top of Summoner's Rift narrows to items available on BOTH —
    // longSword is Summoner's Rift only (maps["12"] == false), so it must drop out.
    viewModel.toggleGameMode(GAME_MODE_ARAM)
    advanceUntilIdle()
    assertEquals(setOf(GAME_MODE_SUMMONERS_RIFT, GAME_MODE_ARAM), viewModel.selectedGameModes.value)
    display = (viewModel.itemListState.value as UiState.Success).data
    assertEquals(listOf(infinityEdge), (display as ItemListDisplay.Flat).items)
    job.cancel()
}
```

- [ ] **Step 4: Update the remaining game-mode tests to call `toggleGameMode` and check `selectedGameModes`**

Replace `gameModeFilter_showsFlatDisplayWithMatchingItems` (lines 217-229): change `viewModel.selectGameMode(GAME_MODE_ARENA)` to `viewModel.toggleGameMode(GAME_MODE_ARENA)`.

Replace `gameModeAndCategoryCombined_mustMatchBoth` (lines 264-279): change `viewModel.selectGameMode(GAME_MODE_ARENA)` to `viewModel.toggleGameMode(GAME_MODE_ARENA)`.

Replace `gameModeAndTagCombined_mustMatchBoth` (lines 281-296): change `viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)` to `viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)`.

Replace `searchText_combinedWithGameMode` (lines 298-311): change `viewModel.selectGameMode(GAME_MODE_ARAM)` to `viewModel.toggleGameMode(GAME_MODE_ARAM)`.

Replace `clearFilters_alsoClearsGameMode` (lines 313-327):

```kotlin
@Test
fun clearFilters_alsoClearsGameMode() = runTest {
    val job = launch { viewModel.itemListState.collect() }
    advanceUntilIdle()

    viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
    advanceUntilIdle()
    viewModel.clearFilters()
    advanceUntilIdle()

    assertTrue(viewModel.selectedGameModes.value.isEmpty())
    val display = (viewModel.itemListState.value as UiState.Success).data
    assertTrue(display is ItemListDisplay.Categorized)
    job.cancel()
}
```

- [ ] **Step 5: Run the full test file to confirm compile errors / failures**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`
Expected: FAIL to compile — `selectedGameModes`, `toggleGameMode` are unresolved references (the ViewModel hasn't changed yet).

- [ ] **Step 6: Update `ItemViewModel.kt`**

Replace the `KEY_SELECTED_GAME_MODE` constant (line 29):

```kotlin
private const val KEY_SELECTED_GAME_MODES = "selected_game_modes"
```

Replace the `selectedGameMode` property (line 108):

```kotlin
val selectedGameModes: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_GAME_MODES, emptySet())
```

Replace the `combine` call and its body (lines 120-144):

```kotlin
combine(_categorizedRawItems, searchQuery, selectedCategories, selectedTags, selectedGameModes) { state, query, categories, tags, gameModes ->
    when (state) {
        is UiState.Loading -> UiState.Loading
        is UiState.Error -> state
        is UiState.Success -> {
            val (groups, categoryByItemId) = state.data
            if (categories.isEmpty() && tags.isEmpty() && gameModes.isEmpty()) {
                val filtered = if (query.isBlank()) groups
                else groups.mapNotNull { (name, items) ->
                    val matched = items.filter { it.name.contains(query, ignoreCase = true) }
                    if (matched.isEmpty()) null else name to matched
                }
                UiState.Success(ItemListDisplay.Categorized(filtered))
            } else {
                val flat = groups.flatMap { it.second }.filter { item ->
                    (categories.isEmpty() || categoryByItemId[item.id] in categories) &&
                        (tags.isEmpty() || tags.all { it in item.tags }) &&
                        (gameModes.isEmpty() || gameModes.all { item.maps[it] == true }) &&
                        (query.isBlank() || item.name.contains(query, ignoreCase = true))
                }
                UiState.Success(ItemListDisplay.Flat(flat))
            }
        }
    }
}
```

Replace `selectGameMode` (lines 178-180):

```kotlin
fun toggleGameMode(mapId: String) {
    val current = selectedGameModes.value
    savedStateHandle[KEY_SELECTED_GAME_MODES] = if (mapId in current) current - mapId else current + mapId
}
```

Replace the game-mode line inside `clearFilters()` (line 185):

```kotlin
savedStateHandle[KEY_SELECTED_GAME_MODES] = emptySet<String>()
```

- [ ] **Step 7: Run the full test suite**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: PASS (all tests, including the new/updated ones in `ItemViewModelTest`).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt
git commit -m "feat: AND-within-group tag/game-mode filtering, multi-select game mode"
```

---

### Task 3: Bottom sheet — multi-select game mode chips

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt`
- Test: `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt`

**Interfaces:**
- Consumes: `selectedGameModes: StateFlow<Set<String>>`, `fun toggleGameMode(mapId: String)` (Task 2).
- Produces: `ItemFilterBottomSheet(..., selectedGameModes: Set<String>, ..., onGameModeToggle: (String) -> Unit, ...)` (replaces `selectedGameMode: String?` / `onGameModeSelect: (String) -> Unit`). Task 4's `ItemRoute` wiring consumes this exact signature.

- [ ] **Step 1: Write the failing tests**

In `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt`, replace `tappingGameModeChip_invokesOnGameModeSelect` (lines 93-116) with:

```kotlin
@Test
fun tappingGameModeChip_invokesOnGameModeToggle() {
    var toggledMode: String? = null

    composeTestRule.setContent {
        TestTheme {
            ItemFilterBottomSheet(
                availableTags = emptyList(),
                selectedCategories = emptySet(),
                selectedTags = emptySet(),
                selectedGameModes = emptySet(),
                onCategoryToggle = {},
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
                selectedCategories = emptySet(),
                selectedTags = emptySet(),
                selectedGameModes = setOf(GAME_MODE_ARAM, GAME_MODE_ARENA),
                onCategoryToggle = {},
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
```

Add these imports at the top of the file:

```kotlin
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import com.zzy.champions.ui.items.GAME_MODE_ARENA
```

Also update the other two existing tests (`tappingTagChip_invokesOnTagToggle` lines 44-66, `tappingClearAll_invokesOnClearAll` lines 68-91) to pass `selectedGameModes = emptySet()` / `onGameModeToggle = {}` instead of `selectedGameMode = null` / `onGameModeSelect = {}`.

- [ ] **Step 2: Run the tests to confirm they fail to compile**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemFilterBottomSheetTest"`
Expected: FAIL to compile — `selectedGameModes`/`onGameModeToggle` are unresolved parameters on `ItemFilterBottomSheet`.

- [ ] **Step 3: Update `ItemFilterBottomSheet.kt`**

Replace the function signature (lines 53-63):

```kotlin
fun ItemFilterBottomSheet(
    availableTags: List<String>,
    selectedCategories: Set<String>,
    selectedTags: Set<String>,
    selectedGameModes: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onGameModeToggle: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
```

Replace the Game Mode `FlowRow` body (lines 103-111):

```kotlin
ALL_GAME_MODES.forEach { mode ->
    val label = gameModeNameResIds[mode]?.let { stringResource(it) } ?: mode
    FilterChip(
        selected = mode in selectedGameModes,
        onClick = { onGameModeToggle(mode) },
        label = { Text(label) },
        colors = goldenFilterChipColors(),
    )
}
```

- [ ] **Step 4: Run the tests to confirm they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemFilterBottomSheetTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt
git commit -m "feat: multi-select game mode chips in filter bottom sheet"
```

---

### Task 4: Active-filter chip row — one dismissible chip per selected mode

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`
- Test: `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt`

**Interfaces:**
- Consumes: `selectedGameModes: StateFlow<Set<String>>`, `fun toggleGameMode(mapId: String)` (Task 2); `ItemFilterBottomSheet(..., selectedGameModes: Set<String>, ..., onGameModeToggle: (String) -> Unit, ...)` (Task 3).
- Produces: `ItemScreen(..., selectedGameModes: Set<String> = emptySet(), ..., onGameModeClear: (String) -> Unit = {}, ...)` (replaces `selectedGameMode: String?` / `onGameModeClear: () -> Unit`).

- [ ] **Step 1: Write the failing tests**

In `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt`, replace the three existing game-mode-chip tests (lines 119-169) with:

```kotlin
@Test
fun activeGameModeChip_rendersWhenSelected() {
    composeTestRule.setContent {
        MyApplicationTheme {
            ItemScreen(
                itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                version = "",
                selectedGameModes = setOf(GAME_MODE_ARAM),
                onItemClick = {},
            )
        }
    }

    composeTestRule.onNodeWithText("ARAM").assertExists()
}

@Test
fun activeGameModeChip_hiddenWhenNoneSelected() {
    composeTestRule.setContent {
        MyApplicationTheme {
            ItemScreen(
                itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                version = "",
                selectedGameModes = emptySet(),
                onItemClick = {},
            )
        }
    }

    composeTestRule.onNodeWithText("ARAM").assertDoesNotExist()
}

@Test
fun activeGameModeChip_clickInvokesOnGameModeClear() {
    var clearedMode: String? = null
    composeTestRule.setContent {
        MyApplicationTheme {
            ItemScreen(
                itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                version = "",
                selectedGameModes = setOf(GAME_MODE_ARAM),
                onGameModeClear = { clearedMode = it },
                onItemClick = {},
            )
        }
    }

    composeTestRule.onNodeWithText("ARAM").performClick()

    assertEquals(GAME_MODE_ARAM, clearedMode)
}

@Test
fun multipleActiveGameModeChips_renderOneChipEach() {
    composeTestRule.setContent {
        MyApplicationTheme {
            ItemScreen(
                itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                version = "",
                selectedGameModes = setOf(GAME_MODE_ARAM, GAME_MODE_SUMMONERS_RIFT),
                onItemClick = {},
            )
        }
    }

    composeTestRule.onNodeWithText("ARAM").assertExists()
    composeTestRule.onNodeWithText("Summoner's Rift").assertExists()
}

@Test
fun clickingOneOfMultipleChips_clearsOnlyThatMode() {
    var clearedMode: String? = null
    composeTestRule.setContent {
        MyApplicationTheme {
            ItemScreen(
                itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                version = "",
                selectedGameModes = setOf(GAME_MODE_ARAM, GAME_MODE_SUMMONERS_RIFT),
                onGameModeClear = { clearedMode = it },
                onItemClick = {},
            )
        }
    }

    composeTestRule.onNodeWithText("ARAM").performClick()

    assertEquals(GAME_MODE_ARAM, clearedMode)
}
```

Add this import at the top of the file:

```kotlin
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
```

Also add `import org.junit.Assert.assertEquals` if not already present (check the existing import list — the file currently only imports `assertTrue`).

- [ ] **Step 2: Run the tests to confirm they fail to compile**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemScreenTest"`
Expected: FAIL to compile — `selectedGameModes` is an unresolved parameter on `ItemScreen`, and `onGameModeClear` still expects `() -> Unit`.

- [ ] **Step 3: Update `ItemScreen.kt`**

Add `ExperimentalLayoutApi` opt-in and `FlowRow`/`Arrangement` imports at the top of the file:

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
```

Update `ItemRoute` (lines 105, 122-125):

```kotlin
val selectedGameModes by viewModel.selectedGameModes.collectAsStateWithLifecycle()
```

```kotlin
isFilterActive = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || selectedGameModes.isNotEmpty(),
onFilterIconClick = { showFilterSheet = true },
selectedGameModes = selectedGameModes,
onGameModeClear = viewModel::toggleGameMode,
```

Update the `ItemFilterBottomSheet` call inside `ItemRoute` (lines 131-141):

```kotlin
ItemFilterBottomSheet(
    availableTags = availableTags,
    selectedCategories = selectedCategories,
    selectedTags = selectedTags,
    selectedGameModes = selectedGameModes,
    onCategoryToggle = viewModel::toggleCategoryFilter,
    onTagToggle = viewModel::toggleTagFilter,
    onGameModeToggle = viewModel::toggleGameMode,
    onClearAll = viewModel::clearFilters,
    onDismiss = { showFilterSheet = false },
)
```

Update the `ItemScreen` function signature (lines 162-177) — add `@OptIn(ExperimentalLayoutApi::class)` above `@Composable`, and change:

```kotlin
selectedGameMode: String? = null,
onGameModeClear: () -> Unit = {},
```

to:

```kotlin
selectedGameModes: Set<String> = emptySet(),
onGameModeClear: (String) -> Unit = {},
```

Replace the active-chip block inside `ItemScreen`'s `Column` (lines 192-198):

```kotlin
if (selectedGameModes.isNotEmpty()) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        selectedGameModes.forEach { mode ->
            ActiveGameModeChip(
                gameMode = mode,
                onClear = { onGameModeClear(mode) },
            )
        }
    }
}
```

`ActiveGameModeChip` itself (lines 259-278) is unchanged — it still takes a single `gameMode: String` and `onClear: () -> Unit`.

- [ ] **Step 4: Run the tests to confirm they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemScreenTest"`
Expected: PASS.

- [ ] **Step 5: Run the full suite**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: PASS (entire suite).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt
git commit -m "feat: render one dismissible chip per selected game mode under search bar"
```
