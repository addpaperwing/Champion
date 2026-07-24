# Item Filter Game Mode Follow-up Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a single-select "Game Mode" filter (Summoner's Rift / ARAM / Arena) to the item page's existing filter bottom sheet, show the active selection as a dismissible chip under the search bar, and fix two real bugs found in manual testing of the already-merged category/tag filter feature: the "Clear all" button is nearly invisible, and the bottom sheet under-uses the app's gold accent color.

**Architecture:** `ItemViewModel` gains a nullable `selectedGameMode: StateFlow<String?>` (single-select — selecting the active mode again clears it) persisted via `SavedStateHandle`, folded into the existing `itemListState` combine as a 5th input with AND semantics against category/tag/search. `ItemFilterBottomSheet` gains a third chip section between Category and Tags. `ItemScreen` gains a new row (an `InputChip`) directly under the search bar, visible only when a game mode is active.

**Tech Stack:** Kotlin 2.1.20, Jetpack Compose (BOM 2025.06.00), Hilt, Robolectric + `createAndroidComposeRule` (existing pattern), MockK, JUnit4. Same toolchain as `docs/specs/2026-07-18-item-filter-plan.md` — this is a direct follow-up to that already-merged feature.

## Global Constraints

- minSdk 24, targetSdk 36 — unaffected by this work.
- No new third-party icon dependency: `Icons.Default.Close` is part of the core `Icons.Default` set already used in this codebase (`SearchBar.kt` imports `Icons.Default.Clear` from the same `androidx.compose.material.icons.filled` package) — no `material-icons-extended` needed.
- New user-facing strings must be added to **all 21** `values*/strings.xml` files (default + cs, de, el, es, fr, hu, in, it, ja, ko, pl, pt-rBR, ro, ru, th, tr, vi, zh-rCN, zh-rMY, zh-rTW) — same full-locale-parity convention as the original filter feature.
- Persist `selectedGameMode` via `SavedStateHandle`, matching the existing `searchQuery`/`selectedCategories`/`selectedTags` pattern in `ItemViewModel.kt`.
- Compose UI tests go in `app/src/test` using `createAndroidComposeRule<ComponentActivity>()` + `@RunWith(AndroidJUnit4::class)` — no new `androidTest` instrumented tests.
- **Known project-specific Compose-test gotcha:** in this project's resolved `androidx.compose.ui.test` (1.8.2), `assertExists`/`assertDoesNotExist` are member methods on `SemanticsNodeInteraction`, not top-level extension functions — `import androidx.compose.ui.test.assertExists` is itself an invalid, non-compiling import. Call `.assertExists()`/`.assertDoesNotExist()` directly with no such import.
- **Known project-specific Robolectric gotcha:** this project pins Robolectric 4.14.1 / SDK 34, which has a real hit-testing bug (robolectric/robolectric#9595) for shaped clickables — `performClick()` silently no-ops on: (a) any clickable inside a `ModalBottomSheet` (its asymmetric corner shape), worked around today in `ItemFilterBottomSheetTest.kt` via a local `TestTheme` composable overriding `MaterialTheme.shapes.extraLarge` to a non-rounded shape; (b) `IconButton`'s `CircleShape` clip, worked around today in `ItemScreenTest.kt` via `@RunWith(AndroidJUnit4::class)` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)` at the class level. Both workarounds already exist in the two test files this plan modifies — reuse them, do not invent new ones or add dependencies to route around either symptom.
- Full design doc: `docs/specs/2026-07-18-item-filter-game-mode-design.md`.

---

### Task 1: Game mode state in `ItemViewModel`

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`
- Modify: `app/src/test/java/com/zzy/champions/TestItemRepository.kt` (differentiate fixture `maps` values so game-mode filtering is actually testable)
- Test: `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`

**Interfaces:**
- Consumes: `TestItemRepository`, `longSword`/`infinityEdge`/`sorceresShoes` fixtures — this task changes their `maps` values (see Step 1).
- Produces (for Tasks 2 & 3):
  - `internal const val GAME_MODE_SUMMONERS_RIFT = "11"`, `GAME_MODE_ARAM = "12"`, `GAME_MODE_ARENA = "22"`
  - `internal val ALL_GAME_MODES: List<String>` (the 3 constants above, in display order)
  - `ItemViewModel.selectedGameMode: StateFlow<String?>`
  - `ItemViewModel.selectGameMode(mapId: String)` — toggles: selecting the currently-active mode clears it (sets `null`), selecting a different mode replaces it.
  - `itemListState` now factors in `selectedGameMode` (AND with category/tag/search; `null` = no restriction).
  - `clearFilters()` now also resets `selectedGameMode` to `null`.

- [ ] **Step 1: Update fixture `maps` values so game modes are distinguishable**

The existing fixtures in `app/src/test/java/com/zzy/champions/TestItemRepository.kt` all share identical `maps = mapOf("11" to true, "12" to true)`, which can't exercise game-mode filtering. Change each item's `maps` value to the following (nothing else in the file changes — same ids, names, tags, gold, etc.):

In `longSword`, change:
```kotlin
    maps = mapOf("11" to true, "12" to true),
```
to:
```kotlin
    maps = mapOf("11" to true, "12" to false, "22" to false), // Summoner's Rift only
```

In `infinityEdge`, change:
```kotlin
    maps = mapOf("11" to true, "12" to true),
```
to:
```kotlin
    maps = mapOf("11" to true, "12" to true, "22" to false), // Summoner's Rift + ARAM
```

In `sorceresShoes`, change:
```kotlin
    maps = mapOf("11" to true, "12" to true),
```
to:
```kotlin
    maps = mapOf("11" to false, "12" to true, "22" to true), // ARAM + Arena
```

No other test currently reads `.maps`, so this is safe — verify by running the full existing suite after this one change:

Run: `.\gradlew.bat testDebugUnitTest`
Expected: BUILD SUCCESSFUL, same pass count as before this change (this step only changes data, not behavior of any existing test).

- [ ] **Step 2: Commit the fixture change on its own**

```bash
git add app/src/test/java/com/zzy/champions/TestItemRepository.kt
git commit -m "test: differentiate item fixture game-mode availability"
```

- [ ] **Step 3: Write the failing/updated ViewModel tests**

Add the following imports to `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt` (alongside the existing `com.zzy.champions.ui.items.*` imports):

```kotlin
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
```

Add these test methods inside the `ItemViewModelTest` class, after the existing `availableTags_excludesTagsMatchingCategoryNames` test (before the closing `}` of the class):

```kotlin
    @Test
    fun selectedGameMode_isNullInitially() {
        assertNull(viewModel.selectedGameMode.value)
    }

    @Test
    fun gameModeFilter_showsFlatDisplayWithMatchingItems() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_ARENA)
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun selectingSameGameModeAgain_clearsSelection() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()

        assertNull(viewModel.selectedGameMode.value)
        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        job.cancel()
    }

    @Test
    fun selectingDifferentGameMode_replacesSelection() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        var display = (viewModel.itemListState.value as UiState.Success).data
        assertEquals(listOf(longSword, infinityEdge), (display as ItemListDisplay.Flat).items)

        viewModel.selectGameMode(GAME_MODE_ARAM)
        advanceUntilIdle()
        display = (viewModel.itemListState.value as UiState.Success).data
        assertEquals(listOf(sorceresShoes, infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun gameModeAndCategoryCombined_mustMatchBoth() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // infinityEdge is the only CATEGORY_LEGENDARY item, but it has no Arena availability —
        // AND semantics must exclude it even though the category alone would match it.
        viewModel.toggleCategoryFilter(CATEGORY_LEGENDARY)
        viewModel.selectGameMode(GAME_MODE_ARENA)
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertTrue((display as ItemListDisplay.Flat).items.isEmpty())
        job.cancel()
    }

    @Test
    fun searchText_combinedWithGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_ARAM)
        viewModel.updateSearchQuery("Sorcerer")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun clearFilters_alsoClearsGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        assertNull(viewModel.selectedGameMode.value)
        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        job.cancel()
    }
```

- [ ] **Step 4: Run tests to verify they fail (compile error)**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`
Expected: FAIL — compile error, `selectedGameMode`/`selectGameMode`/`GAME_MODE_ARAM`/`GAME_MODE_ARENA`/`GAME_MODE_SUMMONERS_RIFT` are unresolved references.

- [ ] **Step 5: Implement the ViewModel changes**

Replace the full contents of `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`:

```kotlin
package com.zzy.champions.ui.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.localVersionState
import com.zzy.champions.domain.GetItemDataUseCase
import com.zzy.champions.util.stateInViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_SEARCH_QUERY = "search_query"
private const val KEY_SELECTED_CATEGORIES = "selected_categories"
private const val KEY_SELECTED_TAGS = "selected_tags"
private const val KEY_SELECTED_GAME_MODE = "selected_game_mode"

internal const val CATEGORY_STARTER    = "Starter"
internal const val CATEGORY_BOOTS      = "Boots"
internal const val CATEGORY_MYTHIC     = "Mythic"
internal const val CATEGORY_LEGENDARY  = "Legendary"
internal const val CATEGORY_COMPONENTS = "Components"
internal const val CATEGORY_EPIC       = "Epic"
internal const val CATEGORY_OTHER      = "Other"

internal val ALL_CATEGORIES = listOf(
    CATEGORY_STARTER,
    CATEGORY_BOOTS,
    CATEGORY_MYTHIC,
    CATEGORY_LEGENDARY,
    CATEGORY_COMPONENTS,
    CATEGORY_EPIC,
    CATEGORY_OTHER,
)

internal const val GAME_MODE_SUMMONERS_RIFT = "11"
internal const val GAME_MODE_ARAM           = "12"
internal const val GAME_MODE_ARENA          = "22"

internal val ALL_GAME_MODES = listOf(
    GAME_MODE_SUMMONERS_RIFT,
    GAME_MODE_ARAM,
    GAME_MODE_ARENA,
)

sealed interface ItemListDisplay {
    data class Categorized(val groups: List<Pair<String, List<Item>>>) : ItemListDisplay
    data class Flat(val items: List<Item>) : ItemListDisplay
}

private data class CategorizedData(
    val groups: List<Pair<String, List<Item>>>,
    val categoryByItemId: Map<String, String>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItemViewModel @Inject constructor(
    private val getItemDataUseCase: GetItemDataUseCase,
    private val appDataRepository: AppDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    private val _rawItems: StateFlow<UiState<List<Item>>> = _retryTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                emit(getItemDataUseCase())
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading, started = SharingStarted.Lazily)

    // Categorization (and the category-by-item-id index derived from it) is computed once
    // from raw items, not re-run on every search keystroke. Both are carried in one state
    // object so they can never drift out of sync with each other across combine emissions.
    private val _categorizedRawItems: StateFlow<UiState<CategorizedData>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    val groups = categorizeItems(state.data)
                    val categoryByItemId = groups.flatMap { (name, items) -> items.map { it.id to name } }.toMap()
                    UiState.Success(CategorizedData(groups, categoryByItemId))
                }
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    val selectedCategories: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_CATEGORIES, emptySet())
    val selectedTags: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_TAGS, emptySet())
    val selectedGameMode: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_SELECTED_GAME_MODE, null)

    val availableTags: StateFlow<List<String>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.flatMap { it.tags }.filter { it !in ALL_CATEGORIES }.distinct().sorted()
                else -> emptyList()
            }
        }
        .stateInViewModel(viewModelScope, initialValue = emptyList())

    val itemListState: StateFlow<UiState<ItemListDisplay>> =
        combine(_categorizedRawItems, searchQuery, selectedCategories, selectedTags, selectedGameMode) { state, query, categories, tags, gameMode ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    val (groups, categoryByItemId) = state.data
                    if (categories.isEmpty() && tags.isEmpty() && gameMode == null) {
                        val filtered = if (query.isBlank()) groups
                        else groups.mapNotNull { (name, items) ->
                            val matched = items.filter { it.name.contains(query, ignoreCase = true) }
                            if (matched.isEmpty()) null else name to matched
                        }
                        UiState.Success(ItemListDisplay.Categorized(filtered))
                    } else {
                        val flat = groups.flatMap { it.second }.filter { item ->
                            (categories.isEmpty() || categoryByItemId[item.id] in categories) &&
                                (tags.isEmpty() || item.tags.any { it in tags }) &&
                                (gameMode == null || item.maps[gameMode] == true) &&
                                (query.isBlank() || item.name.contains(query, ignoreCase = true))
                        }
                        UiState.Success(ItemListDisplay.Flat(flat))
                    }
                }
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    // Lazily (not WhileSubscribed) so the cached value persists after the UI goes to
    // background — prevents a blank version badge flash on return.
    val version: StateFlow<String> = appDataRepository.localVersionState(viewModelScope, started = SharingStarted.Lazily)

    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem.asStateFlow()

    // Survives retry(): keeps the last successful item list so BottomSheet lookups
    // continue to work while a new fetch is in-flight (when _rawItems = Loading).
    private var _lastKnownItems: Map<String, Item> = emptyMap()

    init {
        viewModelScope.launch {
            _rawItems.collect { state ->
                if (state is UiState.Success) _lastKnownItems = state.data.associateBy { it.id }
            }
        }
    }

    fun updateSearchQuery(query: String) { savedStateHandle[KEY_SEARCH_QUERY] = query }

    fun toggleCategoryFilter(category: String) {
        val current = selectedCategories.value
        savedStateHandle[KEY_SELECTED_CATEGORIES] = if (category in current) current - category else current + category
    }

    fun toggleTagFilter(tag: String) {
        val current = selectedTags.value
        savedStateHandle[KEY_SELECTED_TAGS] = if (tag in current) current - tag else current + tag
    }

    fun selectGameMode(mapId: String) {
        savedStateHandle[KEY_SELECTED_GAME_MODE] = if (selectedGameMode.value == mapId) null else mapId
    }

    fun clearFilters() {
        savedStateHandle[KEY_SELECTED_CATEGORIES] = emptySet<String>()
        savedStateHandle[KEY_SELECTED_TAGS] = emptySet<String>()
        savedStateHandle[KEY_SELECTED_GAME_MODE] = null
    }

    fun selectItem(item: Item) { _selectedItem.value = item }
    fun dismissItem() { _selectedItem.value = null }
    fun retry() { _retryTrigger.update { it + 1 } }

    fun getItemById(id: String): Item? = _lastKnownItems[id]
}

internal fun categorizeItems(items: List<Item>): List<Pair<String, List<Item>>> {
    val validItems = items.filter { it.id.isNotEmpty() }
    val allItemIds = validItems.map { it.id }.toSet()
    val componentIds = validItems.flatMap { it.components }.filter { it in allItemIds }.toSet()

    val categories = listOf(
        CATEGORY_STARTER    to { item: Item -> item.gold.total in 1..500 && item.id !in componentIds && "Boots" !in item.tags },
        CATEGORY_BOOTS      to { item: Item -> "Boots" in item.tags },
        CATEGORY_MYTHIC     to { item: Item -> "Mythic" in item.tags },
        CATEGORY_LEGENDARY  to { item: Item -> "Legendary" in item.tags },
        CATEGORY_COMPONENTS to { item: Item -> item.id in componentIds },
        CATEGORY_EPIC       to { item: Item -> item.gold.total >= 1000 },
        CATEGORY_OTHER      to { _: Item -> true },
    )

    val assigned = mutableSetOf<String>()
    return categories.mapNotNull { (name, predicate) ->
        val batch = validItems.filter { it.id !in assigned && predicate(it) }
        if (batch.isEmpty()) null
        else {
            assigned.addAll(batch.map { it.id })
            name to batch
        }
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`
Expected: PASS (19 tests: the 12 existing plus the 7 new ones from Step 3).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt
git commit -m "feat: add single-select game mode filter state to ItemViewModel"
```

---

### Task 2: Game Mode section + gold styling in `ItemFilterBottomSheet`

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt` (add `gameModeNameResIds` only — full rewiring is Task 3)
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt`
- Modify: `app/src/main/res/values/strings.xml` and the 20 locale variants
- Test: `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt`

**Interfaces:**
- Consumes: `GAME_MODE_SUMMONERS_RIFT`, `GAME_MODE_ARAM`, `GAME_MODE_ARENA`, `ALL_GAME_MODES` (Task 1).
- Produces (for Task 3): `internal val gameModeNameResIds: Map<String, Int>` (in `ItemScreen.kt`, consumed by `ItemScreen.kt`'s own `ActiveGameModeChip` in Task 3); `ItemFilterBottomSheet`'s new required parameters `selectedGameMode: String?` and `onGameModeSelect: (String) -> Unit`.

- [ ] **Step 1: Write the failing/updated bottom sheet tests**

Replace the full contents of `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt`:

```kotlin
package com.zzy.champions.items

import androidx.activity.ComponentActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.ui.items.GAME_MODE_ARAM
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
                    selectedCategories = emptySet(),
                    selectedTags = emptySet(),
                    selectedGameMode = null,
                    onCategoryToggle = {},
                    onTagToggle = { toggledTag = it },
                    onGameModeSelect = {},
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
                    selectedCategories = setOf("Boots"),
                    selectedTags = emptySet(),
                    selectedGameMode = null,
                    onCategoryToggle = {},
                    onTagToggle = {},
                    onGameModeSelect = {},
                    onClearAll = { cleared = true },
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clear all").performClick()

        assertTrue(cleared)
    }

    @Test
    fun tappingGameModeChip_invokesOnGameModeSelect() {
        var selectedMode: String? = null

        composeTestRule.setContent {
            TestTheme {
                ItemFilterBottomSheet(
                    availableTags = emptyList(),
                    selectedCategories = emptySet(),
                    selectedTags = emptySet(),
                    selectedGameMode = null,
                    onCategoryToggle = {},
                    onTagToggle = {},
                    onGameModeSelect = { selectedMode = it },
                    onClearAll = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ARAM").performClick()

        assertEquals(GAME_MODE_ARAM, selectedMode)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemFilterBottomSheetTest"`
Expected: FAIL — compile error, `ItemFilterBottomSheet` doesn't have `selectedGameMode`/`onGameModeSelect` parameters, and no chip with text "ARAM" exists yet.

- [ ] **Step 3a: Add `gameModeNameResIds` to `ItemScreen.kt`**

In `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`, add these three imports alongside the existing `com.zzy.champions.ui.items.*` imports:

```kotlin
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
```

Then add this new `internal val` immediately after the existing `categoryNameResIds` map (same file, top level):

```kotlin
internal val gameModeNameResIds = mapOf(
    GAME_MODE_SUMMONERS_RIFT to R.string.game_mode_summoners_rift,
    GAME_MODE_ARAM           to R.string.game_mode_aram,
    GAME_MODE_ARENA          to R.string.game_mode_arena,
)
```

- [ ] **Step 3b: Add the 5 new strings (all 21 locale files)**

In every one of these files, insert the 5 new lines immediately after the existing `<string name="filter_no_results">...</string>` line: `app/src/main/res/values/strings.xml`, `values-cs`, `values-de`, `values-el`, `values-es`, `values-fr`, `values-hu`, `values-in`, `values-it`, `values-ja`, `values-ko`, `values-pl`, `values-pt-rBR`, `values-ro`, `values-ru`, `values-th`, `values-tr`, `values-vi`, `values-zh-rCN`, `values-zh-rMY`, `values-zh-rTW` (each at path `app/src/main/res/<dir>/strings.xml`).

Use this table (English row first, then each locale's translation):

| Locale dir | filter_game_mode | game_mode_summoners_rift | game_mode_aram | game_mode_arena | clear_game_mode_filter |
|---|---|---|---|---|---|
| values | Game Mode | Summoner's Rift | ARAM | Arena | Clear game mode filter |
| values-cs | Herní režim | Zaklínačova rokle | ARAM | Aréna | Vymazat filtr herního režimu |
| values-de | Spielmodus | Klüfte der Beschwörer | ARAM | Arena | Spielmodusfilter löschen |
| values-el | Λειτουργία παιχνιδιού | Χαράδρα του Επικαλεστή | ARAM | Αρένα | Απαλοιφή φίλτρου λειτουργίας παιχνιδιού |
| values-es | Modo de juego | Grieta del Invocador | ARAM | Arena | Borrar filtro de modo de juego |
| values-fr | Mode de jeu | Faille de l'invocateur | ARAM | Arène | Effacer le filtre de mode de jeu |
| values-hu | Játékmód | Idézőmező | ARAM | Aréna | Játékmód-szűrő törlése |
| values-in | Mode permainan | Summoner's Rift | ARAM | Arena | Hapus filter mode permainan |
| values-it | Modalità di gioco | Landa degli Evocatori | ARAM | Arena | Cancella filtro modalità di gioco |
| values-ja | ゲームモード | サモナーズリフト | ARAM | アリーナ | ゲームモードフィルターをクリア |
| values-ko | 게임 모드 | 소환사의 협곡 | ARAM | 아레나 | 게임 모드 필터 지우기 |
| values-pl | Tryb gry | Bagno Przyzywaczy | ARAM | Arena | Wyczyść filtr trybu gry |
| values-pt-rBR | Modo de jogo | Fenda do Invocador | ARAM | Arena | Limpar filtro de modo de jogo |
| values-ro | Mod de joc | Fisura Invocatorului | ARAM | Arenă | Șterge filtrul modului de joc |
| values-ru | Режим игры | Расселина призывателя | ARAM | Арена | Очистить фильтр режима игры |
| values-th | โหมดเกม | Summoner's Rift | ARAM | สนามประลอง | ล้างตัวกรองโหมดเกม |
| values-tr | Oyun modu | Çağırıcı Vadisi | ARAM | Arena | Oyun modu filtresini temizle |
| values-vi | Chế độ chơi | Đấu Trường Công Lý | ARAM | Đấu trường | Xóa bộ lọc chế độ chơi |
| values-zh-rCN | 游戏模式 | 召唤师峡谷 | 大乱斗 | 竞技场 | 清除游戏模式筛选 |
| values-zh-rMY | 游戏模式 | 召唤师峡谷 | 大乱斗 | 竞技场 | 清除游戏模式筛选 |
| values-zh-rTW | 遊戲模式 | 召喚師峽谷 | 大亂鬥 | 競技場 | 清除遊戲模式篩選 |

For each file, the inserted block (substituting that row's values) is:

```xml
    <string name="filter_game_mode">Game Mode</string>
    <string name="game_mode_summoners_rift">Summoner\'s Rift</string>
    <string name="game_mode_aram">ARAM</string>
    <string name="game_mode_arena">Arena</string>
    <string name="clear_game_mode_filter">Clear game mode filter</string>
```

(shown here with the English values — note the escaped apostrophe `\'` in `Summoner\'s Rift`, required by Android string XML; use the corresponding row from the table above for every other locale file, escaping any apostrophes in that locale's translation the same way).

- [ ] **Step 3c: Update `ItemFilterBottomSheet.kt`**

Replace the full contents of `app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt`:

```kotlin
package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.ui.items.ALL_CATEGORIES
import com.zzy.champions.ui.items.ALL_GAME_MODES
import com.zzy.champions.ui.theme.Golden

@Composable
fun FilterIconButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_filter_list),
            contentDescription = stringResource(R.string.filter_items),
            tint = if (isActive) Golden else MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun goldenFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = Golden.copy(alpha = 0.25f),
    selectedLabelColor = Golden,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemFilterBottomSheet(
    availableTags: List<String>,
    selectedCategories: Set<String>,
    selectedTags: Set<String>,
    selectedGameMode: String?,
    onCategoryToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onGameModeSelect: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.filter_category),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ALL_CATEGORIES.forEach { category ->
                    val label = categoryNameResIds[category]?.let { stringResource(it) } ?: category
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = { onCategoryToggle(category) },
                        label = { Text(label) },
                        colors = goldenFilterChipColors(),
                    )
                }
            }

            Text(
                text = stringResource(R.string.filter_game_mode),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ALL_GAME_MODES.forEach { mode ->
                    val label = gameModeNameResIds[mode]?.let { stringResource(it) } ?: mode
                    FilterChip(
                        selected = mode == selectedGameMode,
                        onClick = { onGameModeSelect(mode) },
                        label = { Text(label) },
                        colors = goldenFilterChipColors(),
                    )
                }
            }

            Text(
                text = stringResource(R.string.filter_tags),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag) },
                        colors = goldenFilterChipColors(),
                    )
                }
            }

            TextButton(
                onClick = onClearAll,
                colors = ButtonDefaults.textButtonColors(contentColor = Golden),
            ) {
                Text(stringResource(R.string.filter_clear_all))
            }
        }
    }
}
```

Note: `categoryNameResIds` and `gameModeNameResIds` are referenced here with no import — both are `internal val`s in `ItemScreen.kt`, and this file is in the same package (`com.zzy.champions.ui.items.compose`), so no import is needed (same as the existing `categoryNameResIds` usage before this change).

- [ ] **Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemFilterBottomSheetTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Verify the locale XML is well-formed**

Run: `.\gradlew.bat :app:processDebugResources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt app/src/main/res/values*/strings.xml app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt
git commit -m "feat: add game mode chip section and gold accents to filter bottom sheet"
```

---

### Task 3: Wire active game mode chip into `ItemScreen`/`ItemRoute`

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`
- Test: `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt`

**Interfaces:**
- Consumes: `selectedGameMode`/`selectGameMode` (Task 1); `gameModeNameResIds`, `ItemFilterBottomSheet`'s new params (Task 2).
- Produces: nothing further consumed by other tasks — this is the final integration point.

- [ ] **Step 1: Write the failing test**

Add these three test methods to `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt`, after the existing `nonEmptyFlatDisplay_doesNotShowNoResultsMessage` test (before the closing `}` of the class). Also add this import alongside the file's existing imports:

```kotlin
import com.zzy.champions.ui.items.GAME_MODE_ARAM
```

```kotlin
    @Test
    fun activeGameModeChip_rendersWhenSelected() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ItemScreen(
                    itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                    version = "",
                    selectedGameMode = GAME_MODE_ARAM,
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
                    selectedGameMode = null,
                    onItemClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ARAM").assertDoesNotExist()
    }

    @Test
    fun activeGameModeChip_clickInvokesOnGameModeClear() {
        var cleared = false
        composeTestRule.setContent {
            MyApplicationTheme {
                ItemScreen(
                    itemListState = UiState.Success(ItemListDisplay.Flat(emptyList())),
                    version = "",
                    selectedGameMode = GAME_MODE_ARAM,
                    onGameModeClear = { cleared = true },
                    onItemClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ARAM").performClick()

        assertTrue(cleared)
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemScreenTest"`
Expected: FAIL — compile error, `ItemScreen` has no `selectedGameMode`/`onGameModeClear` parameters.

- [ ] **Step 3: Rewrite `ItemScreen.kt`**

Replace the full contents of `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`:

```kotlin
package com.zzy.champions.ui.items.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.R
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.compose.LoadingAndErrorScreen
import com.zzy.champions.ui.index.compose.SearchTextField
import com.zzy.champions.ui.items.CATEGORY_BOOTS
import com.zzy.champions.ui.items.CATEGORY_COMPONENTS
import com.zzy.champions.ui.items.CATEGORY_EPIC
import com.zzy.champions.ui.items.CATEGORY_LEGENDARY
import com.zzy.champions.ui.items.CATEGORY_MYTHIC
import com.zzy.champions.ui.items.CATEGORY_OTHER
import com.zzy.champions.ui.items.CATEGORY_STARTER
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
import com.zzy.champions.ui.items.ItemListDisplay
import com.zzy.champions.ui.items.ItemViewModel
import com.zzy.champions.ui.theme.Golden

private const val GRID_COLUMNS = 5
internal val itemCutCornerShape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
private val categoryHeaderBrush = Brush.horizontalGradient(listOf(Golden.copy(alpha = 0.25f), Color.Transparent))

internal val categoryNameResIds = mapOf(
    CATEGORY_STARTER    to R.string.category_starter,
    CATEGORY_BOOTS      to R.string.category_boots,
    CATEGORY_MYTHIC     to R.string.category_mythic,
    CATEGORY_LEGENDARY  to R.string.category_legendary,
    CATEGORY_COMPONENTS to R.string.category_components,
    CATEGORY_EPIC       to R.string.category_epic,
    CATEGORY_OTHER      to R.string.category_other,
)

internal val gameModeNameResIds = mapOf(
    GAME_MODE_SUMMONERS_RIFT to R.string.game_mode_summoners_rift,
    GAME_MODE_ARAM           to R.string.game_mode_aram,
    GAME_MODE_ARENA          to R.string.game_mode_arena,
)

@Composable
fun ItemRoute(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel(),
    refreshStamp: Int = 0,
    onStampConsumed: () -> Unit = {},
) {
    LaunchedEffect(refreshStamp) {
        if (refreshStamp > 0) {
            viewModel.retry()
            onStampConsumed()
        }
    }

    val itemListState by viewModel.itemListState.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val version by viewModel.version.collectAsStateWithLifecycle()
    val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    val selectedGameMode by viewModel.selectedGameMode.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = searchText.isNotBlank()) {
        viewModel.updateSearchQuery("")
    }

    ItemScreen(
        modifier = modifier,
        itemListState = itemListState,
        version = version,
        searchText = searchText,
        onSearchTextChange = { viewModel.updateSearchQuery(it) },
        onSearchDone = { keyboardController?.hide() },
        onClearSearch = { viewModel.updateSearchQuery("") },
        isFilterActive = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || selectedGameMode != null,
        onFilterIconClick = { showFilterSheet = true },
        selectedGameMode = selectedGameMode,
        onGameModeClear = { selectedGameMode?.let(viewModel::selectGameMode) },
        onItemClick = viewModel::selectItem,
        onReloadClick = viewModel::retry,
    )

    if (showFilterSheet) {
        ItemFilterBottomSheet(
            availableTags = availableTags,
            selectedCategories = selectedCategories,
            selectedTags = selectedTags,
            selectedGameMode = selectedGameMode,
            onCategoryToggle = viewModel::toggleCategoryFilter,
            onTagToggle = viewModel::toggleTagFilter,
            onGameModeSelect = viewModel::selectGameMode,
            onClearAll = viewModel::clearFilters,
            onDismiss = { showFilterSheet = false },
        )
    }

    val resolveItem = remember(viewModel) { viewModel::getItemById }
    val onComponentClick = remember(viewModel) { { componentId: String ->
        val resolved = resolveItem(componentId)
        if (resolved != null) viewModel.selectItem(resolved)
    } }
    // Don't overlay the error screen: dismiss the sheet when the load fails so
    // the user can reach the reload button.
    selectedItem?.takeIf { itemListState !is UiState.Error }?.let { item ->
        ItemBottomSheet(
            item = item,
            version = version,
            onDismiss = viewModel::dismissItem,
            onComponentClick = onComponentClick,
            resolveItem = resolveItem,
        )
    }
}

@Composable
fun ItemScreen(
    modifier: Modifier = Modifier,
    itemListState: UiState<ItemListDisplay>,
    version: String,
    searchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    onSearchDone: () -> Unit = {},
    onClearSearch: (() -> Unit)? = null,
    isFilterActive: Boolean = false,
    onFilterIconClick: () -> Unit = {},
    selectedGameMode: String? = null,
    onGameModeClear: () -> Unit = {},
    onItemClick: (Item) -> Unit,
    onReloadClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        SearchTextField(
            text = searchText,
            onTextChanged = onSearchTextChange,
            onClearText = onClearSearch,
            onDone = { onSearchDone() },
            trailingContent = {
                FilterIconButton(isActive = isFilterActive, onClick = onFilterIconClick)
            },
        )
        if (selectedGameMode != null) {
            ActiveGameModeChip(
                gameMode = selectedGameMode,
                onClear = onGameModeClear,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        when (itemListState) {
            is UiState.Loading -> LoadingAndErrorScreen(
                isLoading = true,
                isError = false,
                onReloadClick = onReloadClick,
            )
            is UiState.Error -> LoadingAndErrorScreen(
                isLoading = false,
                isError = true,
                onReloadClick = onReloadClick,
            )
            is UiState.Success -> {
                val display = itemListState.data
                if (display is ItemListDisplay.Flat && display.items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = stringResource(id = R.string.filter_no_results))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GRID_COLUMNS),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        when (display) {
                            is ItemListDisplay.Categorized -> {
                                display.groups.forEach { (categoryName, categoryItems) ->
                                    item(span = { GridItemSpan(GRID_COLUMNS) }, contentType = "header") {
                                        CategoryHeader(name = categoryName)
                                    }
                                    items(categoryItems, key = { it.id }, contentType = { "item" }) { item ->
                                        ItemCard(
                                            item = item,
                                            version = version,
                                            onClick = { onItemClick(item) },
                                        )
                                    }
                                }
                            }
                            is ItemListDisplay.Flat -> {
                                items(display.items, key = { it.id }, contentType = { "item" }) { item ->
                                    ItemCard(
                                        item = item,
                                        version = version,
                                        onClick = { onItemClick(item) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveGameModeChip(gameMode: String, onClear: () -> Unit, modifier: Modifier = Modifier) {
    val label = gameModeNameResIds[gameMode]?.let { stringResource(it) } ?: gameMode
    InputChip(
        selected = true,
        onClick = onClear,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.clear_game_mode_filter),
            )
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = Golden.copy(alpha = 0.25f),
            selectedLabelColor = Golden,
            selectedTrailingIconColor = Golden,
        ),
        modifier = modifier,
    )
}

@Composable
private fun CategoryHeader(name: String, modifier: Modifier = Modifier) {
    val localizedName = categoryNameResIds[name]?.let { stringResource(it) } ?: name
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(top = 10.dp, bottom = 4.dp)
            .border(Dp.Hairline, Golden, itemCutCornerShape)
            .background(categoryHeaderBrush, itemCutCornerShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = localizedName.uppercase(),
            color = Golden,
            fontWeight = FontWeight(700),
            fontSize = 11.sp,
            letterSpacing = 1.sp,
        )
    }
}
```

If `InputChipDefaults.inputChipColors(...)` or any other Material3 API in this step doesn't compile exactly as shown (unresolved reference on the function or a named parameter), do not guess a replacement or add a dependency — check the actual members available on `InputChipDefaults`/`FilterChipDefaults` via the compiler's error message or IDE autocomplete, and use the closest equivalent (e.g., a differently-named color parameter). Report the discrepancy in your self-review notes either way.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemScreenTest"`
Expected: PASS (6 tests: the 3 existing plus the 3 new ones from Step 1).

- [ ] **Step 5: Run the full unit test suite**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: BUILD SUCCESSFUL — no regressions anywhere (particularly `ItemViewModelTest`, `ItemFilterBottomSheetTest`, `ItemCategorizationTest`, `SearchBarTest`, `ChampionIndexScreenshotTest`/`ChampionViewModelTest`).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt
git commit -m "feat: show active game mode filter as a dismissible chip under the search bar"
```

---

## Self-Review Notes

- **Spec coverage:** curated fixed 3-mode list (Task 1's `ALL_GAME_MODES`), single-select toggle semantics (Task 1's `selectGameMode`), AND with category/tag/search (Task 1's `itemListState`), Game Mode section in bottom sheet between Category and Tags (Task 2), active chip under search bar (Task 3), `Clear all` contrast fix + gold `FilterChip`/section styling (Task 2), full 21-locale parity for 5 new strings (Task 2) — every design doc section has a corresponding task.
- **Placeholder scan:** no TBD/TODO; every step has runnable code and exact commands. One deliberate exception: Task 3 Step 3 includes an explicit fallback instruction for the `InputChipDefaults` API surface, since this project has twice already hit real Compose-API-naming surprises in this exact feature area (documented in Global Constraints) — this is guidance for a genuine, previously-observed risk, not an unresolved placeholder.
- **Type consistency:** `GAME_MODE_SUMMONERS_RIFT`/`GAME_MODE_ARAM`/`GAME_MODE_ARENA`/`ALL_GAME_MODES` (Task 1) match exactly where consumed in Tasks 2 and 3. `selectedGameMode: String?` and `selectGameMode(mapId: String)` (Task 1) match the parameter names/types used in `ItemFilterBottomSheet` (Task 2) and `ItemRoute`/`ItemScreen` (Task 3). `gameModeNameResIds` is introduced once (Task 2, inside `ItemScreen.kt`) and consumed identically in both `ItemFilterBottomSheet.kt` (Task 2, no import needed — same package) and `ItemScreen.kt`'s own `ActiveGameModeChip` (Task 3, same file).
- **Deviation from design doc:** none — this plan implements the approved design as specified, including the concrete `Golden.copy(alpha = 0.25f)` / `Golden` color values the design doc's self-review pinned down.
