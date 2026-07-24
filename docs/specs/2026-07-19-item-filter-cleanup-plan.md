# Item Filter Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hide items unavailable on every game mode, remove category as a filterable dimension (keeping it as a display grouping), and keep category headers visible while filtering — hiding only categories left with zero matching items.

**Architecture:** Both changes live in `ItemViewModel.kt`'s existing reactive pipeline (`_rawItems` → `_categorizedRawItems` → `itemListState`). Availability filtering is a `.filter` added at the `_rawItems` source so it cascades to every downstream consumer for free. Category-filter removal collapses `ItemListDisplay` from a 2-variant sealed interface to a single data class and removes one dimension from the `itemListState` combine.

**Tech Stack:** Kotlin, Jetpack Compose, ViewModel + `SavedStateHandle`-backed `StateFlow`, JUnit4 + Robolectric (Compose UI tests) + MockK (ViewModel tests).

## Global Constraints

- Hide an item from the entire item list when `maps.values.none { it }` (no map ID is `true` — covers both an empty and an all-false `maps` map). Filter at the `_rawItems` source so grouping, `availableTags`, and component/upgrade chip resolution (`getItemById`) all inherit the exclusion.
- The grid is always grouped by category. Search/tag/game-mode filters narrow the items within each category group; a group left with zero matching items is omitted (header included). There is no more ungrouped/flat display mode.
- Category is no longer a filter dimension: no `selectedCategories` state, no category chips in the bottom sheet, no `onCategoryToggle`. `categorizeItems()` and category headers/labels are unaffected.
- No change to tag/game-mode filter semantics (AND-within-group, AND across groups, as already shipped).
- No change to `Item`, Room schema, network layer, or the curated three-mode game-mode list.

Reference: `docs/specs/2026-07-19-item-filter-cleanup-design.md`

---

### Task 1: Hide items unavailable on every map

**Files:**
- Modify: `app/src/test/java/com/zzy/champions/TestItemRepository.kt`
- Modify: `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt:79-86`

**Interfaces:**
- Consumes: existing `Item.maps: Map<String, Boolean>` (`app/src/main/java/com/zzy/champions/data/model/Item.kt:19`), existing `_rawItems: StateFlow<UiState<List<Item>>>` (`ItemViewModel.kt:79`).
- Produces: `_rawItems` now excludes items with no `true` map entry. Every existing consumer of `_rawItems` (`_categorizedRawItems`, `availableTags`, `_lastKnownItems`/`getItemById`) inherits this for free — no signature changes, so Task 2 is unaffected by this task's internals.

- [ ] **Step 1: Add an "unavailable everywhere" fixture item**

In `app/src/test/java/com/zzy/champions/TestItemRepository.kt`, add a fourth fixture item after `sorceresShoes` (line 48):

```kotlin
internal val retiredTrinket = Item(
    id = "9999",
    name = "Retired Trinket",
    description = "No longer purchasable anywhere",
    plaintext = "Removed from all game modes",
    image = Image("9999.png"),
    gold = ItemGold(base = 0, purchasable = false, total = 0, sell = 0),
    tags = listOf("Trinket"),
    maps = mapOf("11" to false, "12" to false, "22" to false, "30" to false), // unavailable on every map
    stats = emptyMap(),
    components = emptyList(),
    upgrades = emptyList(),
)
```

Then update line 50 (the `remoteItems` list) and line 54 (`TestItemRepository`'s `localItems` list) to include it:

```kotlin
private val remoteItems = listOf(longSword, infinityEdge, sorceresShoes, retiredTrinket)

internal class TestItemRepository : ItemRepository {
    // Separate local store so clearning local doesn't affect remote
    private val localItems = mutableListOf(longSword, infinityEdge, sorceresShoes, retiredTrinket)
```

- [ ] **Step 2: Write failing tests for the exclusion**

In `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`, add `retiredTrinket` to the import block (alongside `infinityEdge`, `longSword`, `sorceresShoes` at lines 10-12):

```kotlin
import com.zzy.champions.retiredTrinket
```

Then add these three tests (e.g. after `dismissItem_clearsSelectedItem`, around line 93):

`ItemListDisplay` is still today's sealed interface (`Categorized`/`Flat`) at
this point in the plan — Task 2 is the one that collapses it to a plain
`.groups` property. Write these tests against today's API so the module
keeps compiling; Task 2's Step 1 will replace this whole file (including
these three tests) with versions using the simplified type:

```kotlin
    @Test
    fun unavailableItem_excludedFromItemListState() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        val allItems = (display as ItemListDisplay.Categorized).groups.flatMap { it.second }
        assertTrue(retiredTrinket !in allItems)
        assertEquals(3, allItems.size)
        job.cancel()
    }

    @Test
    fun unavailableItem_excludedFromAvailableTags() = runTest {
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        assertTrue("Trinket" !in viewModel.availableTags.value)
        job.cancel()
    }

    @Test
    fun unavailableItem_getItemByIdReturnsNull() = runTest {
        advanceUntilIdle()

        assertNull(viewModel.getItemById(retiredTrinket.id))
    }
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`

Expected: FAIL — `unavailableItem_excludedFromItemListState` and `items_loadsSuccessfully` both fail (`retiredTrinket` shows up, `totalItems` is 4 instead of 3); `unavailableItem_excludedFromAvailableTags` fails (`"Trinket"` is present); `unavailableItem_getItemByIdReturnsNull` fails (returns the item, not `null`).

- [ ] **Step 4: Implement the availability filter**

In `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`, replace lines 79-86:

```kotlin
    private val _rawItems: StateFlow<UiState<List<Item>>> = _retryTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                emit(getItemDataUseCase())
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading, started = SharingStarted.Lazily)
```

with:

```kotlin
    private val _rawItems: StateFlow<UiState<List<Item>>> = _retryTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                emit(
                    when (val result = getItemDataUseCase()) {
                        is UiState.Success -> UiState.Success(result.data.filter { it.isAvailableOnAnyMap() })
                        else -> result
                    },
                )
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading, started = SharingStarted.Lazily)
```

Then add this private extension function near the bottom of the file, just above `internal fun categorizeItems(...)` (line 196):

```kotlin
private fun Item.isAvailableOnAnyMap() = maps.values.any { it }

```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`

Expected: PASS — all tests in the file, including the three new ones and the pre-existing `items_loadsSuccessfully`/`availableTags_derivedFromLoadedItems`/`availableTags_excludesTagsMatchingCategoryNames`.

- [ ] **Step 6: Commit**

```bash
git add app/src/test/java/com/zzy/champions/TestItemRepository.kt app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt
git commit -m "feat: hide items unavailable on every map"
```

---

### Task 2: Always-categorized display, remove category filter dimension

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`
- Modify: `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`
- Modify: `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt`
- Modify: `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt`
- Modify: 22 `app/src/main/res/values*/strings.xml` files (remove unreferenced `filter_category` string)

**Interfaces:**
- Consumes: `_rawItems` from Task 1 (unchanged signature), `categorizeItems()` (`ItemViewModel.kt:196`, unchanged).
- Produces: `data class ItemListDisplay(val groups: List<Pair<String, List<Item>>>)` (replaces the sealed interface) — consumed by `ItemScreen`, `ItemScreenTest`, `ItemViewModelTest`. `ItemViewModel.itemListState: StateFlow<UiState<ItemListDisplay>>` (same name/type shape as before, new payload). `selectedCategories`, `toggleCategoryFilter`, `KEY_SELECTED_CATEGORIES` no longer exist anywhere.

- [ ] **Step 1: Rewrite `ItemViewModelTest.kt` for the new API**

Replace the full contents of `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt` with:

```kotlin
package com.zzy.champions.items

import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestItemRepository
import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.domain.GetItemDataUseCase
import com.zzy.champions.infinityEdge
import com.zzy.champions.longSword
import com.zzy.champions.retiredTrinket
import com.zzy.champions.sorceresShoes
import com.zzy.champions.ui.items.CATEGORY_BOOTS
import com.zzy.champions.ui.items.CATEGORY_LEGENDARY
import com.zzy.champions.ui.items.CATEGORY_STARTER
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
import com.zzy.champions.ui.items.ItemListDisplay
import com.zzy.champions.ui.items.ItemViewModel
import androidx.lifecycle.SavedStateHandle
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @MockK private lateinit var appDataRepository: AppDataRepository
    private lateinit var itemRepository: TestItemRepository
    private lateinit var useCase: GetItemDataUseCase
    private lateinit var viewModel: ItemViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        coEvery { appDataRepository.getLanguage() } returns flowOf(LANGUAGE_US)
        itemRepository = TestItemRepository()
        useCase = GetItemDataUseCase(itemRepository, appDataRepository, Dispatchers.Main)
        viewModel = ItemViewModel(useCase, appDataRepository, SavedStateHandle())
    }

    private fun groupsNow(): List<Pair<String, List<com.zzy.champions.data.model.Item>>> =
        (viewModel.itemListState.value as UiState.Success).data.groups

    @Test
    fun stateIsInitiallyLoading() {
        assertEquals(UiState.Loading, viewModel.itemListState.value)
    }

    @Test
    fun items_loadsSuccessfully() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_BOOTS, CATEGORY_LEGENDARY), groups.map { it.first })
        assertEquals(3, groups.sumOf { it.second.size })
        job.cancel()
    }

    @Test
    fun unavailableItem_excludedFromItemListState() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        val allItems = groupsNow().flatMap { it.second }
        assertTrue(retiredTrinket !in allItems)
        assertEquals(3, allItems.size)
        job.cancel()
    }

    @Test
    fun unavailableItem_excludedFromAvailableTags() = runTest {
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        assertTrue("Trinket" !in viewModel.availableTags.value)
        job.cancel()
    }

    @Test
    fun unavailableItem_getItemByIdReturnsNull() = runTest {
        advanceUntilIdle()

        assertNull(viewModel.getItemById(retiredTrinket.id))
    }

    @Test
    fun selectedItem_isNullInitially() {
        assertNull(viewModel.selectedItem.value)
    }

    @Test
    fun selectItem_updatesSelectedItem() = runTest {
        viewModel.selectItem(infinityEdge)
        assertEquals(infinityEdge, viewModel.selectedItem.value)
    }

    @Test
    fun dismissItem_clearsSelectedItem() = runTest {
        viewModel.selectItem(infinityEdge)
        viewModel.dismissItem()
        assertNull(viewModel.selectedItem.value)
    }

    @Test
    fun tagFilter_hidesEmptyCategoriesButKeepsMatchingOnes() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // Only longSword and infinityEdge carry "Damage"; sorceresShoes doesn't, so
        // CATEGORY_BOOTS (its only member) must drop out entirely, header included,
        // while CATEGORY_STARTER and CATEGORY_LEGENDARY keep their (unfiltered) members.
        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()

        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_LEGENDARY), groups.map { it.first })
        assertEquals(listOf(longSword), groups.first { it.first == CATEGORY_STARTER }.second)
        assertEquals(listOf(infinityEdge), groups.first { it.first == CATEGORY_LEGENDARY }.second)
        job.cancel()
    }

    @Test
    fun multipleTagsSelected_matchesOnlyItemWithBothTags() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        viewModel.toggleTagFilter("CriticalStrike")
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_LEGENDARY to listOf(infinityEdge)), groupsNow())
        job.cancel()
    }

    @Test
    fun multipleTagsSelected_excludesItemsMissingOneTag() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // sorceresShoes has "Boots" but not "CriticalStrike"; infinityEdge has "CriticalStrike"
        // but not "Boots". Neither item has both, so AND semantics must exclude both, leaving
        // every group empty.
        viewModel.toggleTagFilter("Boots")
        viewModel.toggleTagFilter("CriticalStrike")
        advanceUntilIdle()

        assertTrue(groupsNow().isEmpty())
        job.cancel()
    }

    @Test
    fun clearFilters_restoresAllCategories() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_BOOTS, CATEGORY_LEGENDARY), groups.map { it.first })
        assertEquals(3, groups.sumOf { it.second.size })
        job.cancel()
    }

    @Test
    fun searchText_narrowsActiveTagFilter() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        viewModel.updateSearchQuery("Infinity")
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_LEGENDARY to listOf(infinityEdge)), groupsNow())
        job.cancel()
    }

    @Test
    fun availableTags_derivedFromLoadedItems() = runTest {
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        assertEquals(
            listOf("CriticalStrike", "Damage", "SpellDamage"),
            viewModel.availableTags.value,
        )
        job.cancel()
    }

    @Test
    fun availableTags_excludesTagsMatchingCategoryNames() = runTest {
        // sorceresShoes carries the raw tag "Boots" and infinityEdge carries "Legendary" —
        // both are also fixed category names, so they should not appear as separate
        // "raw tag" chips alongside the category headers of the same name.
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        val tags = viewModel.availableTags.value
        assertTrue("Boots" !in tags)
        assertTrue("Legendary" !in tags)
        assertTrue("Damage" in tags)
        assertTrue("CriticalStrike" in tags)
        assertTrue("SpellDamage" in tags)
        job.cancel()
    }

    @Test
    fun selectedGameModes_isEmptyInitially() {
        assertTrue(viewModel.selectedGameModes.value.isEmpty())
    }

    @Test
    fun gameModeFilter_hidesEmptyCategoriesButKeepsMatchingOnes() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_ARENA)
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_BOOTS to listOf(sorceresShoes)), groupsNow())
        job.cancel()
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
        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_BOOTS, CATEGORY_LEGENDARY), groups.map { it.first })
        job.cancel()
    }

    @Test
    fun togglingTwoGameModes_bothStaySelectedAndMustBothMatch() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        assertEquals(
            listOf(CATEGORY_STARTER to listOf(longSword), CATEGORY_LEGENDARY to listOf(infinityEdge)),
            groupsNow(),
        )

        // Adding ARAM on top of Summoner's Rift narrows to items available on BOTH —
        // longSword is Summoner's Rift only (maps["12"] == false), so it must drop out,
        // taking CATEGORY_STARTER's header with it.
        viewModel.toggleGameMode(GAME_MODE_ARAM)
        advanceUntilIdle()
        assertEquals(setOf(GAME_MODE_SUMMONERS_RIFT, GAME_MODE_ARAM), viewModel.selectedGameModes.value)
        assertEquals(listOf(CATEGORY_LEGENDARY to listOf(infinityEdge)), groupsNow())
        job.cancel()
    }

    @Test
    fun gameModeAndTagCombined_mustMatchBoth() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // sorceresShoes is the only item tagged "SpellDamage", but it has no Summoner's Rift
        // availability — AND semantics must exclude it even though the tag alone would match it.
        viewModel.toggleTagFilter("SpellDamage")
        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()

        assertTrue(groupsNow().isEmpty())
        job.cancel()
    }

    @Test
    fun searchText_combinedWithGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_ARAM)
        viewModel.updateSearchQuery("Sorcerer")
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_BOOTS to listOf(sorceresShoes)), groupsNow())
        job.cancel()
    }

    @Test
    fun clearFilters_alsoClearsGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        assertTrue(viewModel.selectedGameModes.value.isEmpty())
        val groups = groupsNow()
        assertEquals(3, groups.sumOf { it.second.size })
        job.cancel()
    }
}
```

This removes `categoryFilter_showsFlatDisplayWithMatchingItems`,
`categoryAndTagCombined_mustMatchBoth`, and
`gameModeAndCategoryCombined_mustMatchBoth` (the feature they tested no
longer exists), and rewrites every other filtering test to assert against
`ItemListDisplay(groups)` directly instead of branching on
`Categorized`/`Flat`.

- [ ] **Step 2: Run tests to verify they fail to compile**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`

Expected: FAIL to compile — `ItemListDisplay` has no `.groups` property yet (it's still the `Categorized`/`Flat` sealed interface from Task 1), and `CATEGORY_STARTER` needs importing (already added above). This compile failure is the expected "red" state for this refactor.

- [ ] **Step 3: Implement the `ItemViewModel.kt` changes**

In `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`, replace lines 26-29 (the `private const val KEY_*` block) with:

```kotlin
private const val KEY_SEARCH_QUERY = "search_query"
private const val KEY_SELECTED_TAGS = "selected_tags"
private const val KEY_SELECTED_GAME_MODES = "selected_game_modes"
```

Replace lines 59-67 (`sealed interface ItemListDisplay { ... }` and `private data class CategorizedData(...)`) with:

```kotlin
data class ItemListDisplay(val groups: List<Pair<String, List<Item>>>)
```

Replace lines 88-103 (the `_categorizedRawItems` block, including its leading comment) with:

```kotlin
    private val _categorizedRawItems: StateFlow<UiState<List<Pair<String, List<Item>>>>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> UiState.Success(categorizeItems(state.data))
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)
```

Replace line 106 (`val selectedCategories: StateFlow<Set<String>> = ...`) by deleting it — the surrounding block (lines 105-108) becomes:

```kotlin
    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    val selectedTags: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_TAGS, emptySet())
    val selectedGameModes: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_GAME_MODES, emptySet())
```

Replace lines 119-145 (the `itemListState` combine) with:

```kotlin
    val itemListState: StateFlow<UiState<ItemListDisplay>> =
        combine(_categorizedRawItems, searchQuery, selectedTags, selectedGameModes) { state, query, tags, gameModes ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    val filtered = state.data.mapNotNull { (name, items) ->
                        val matched = items.filter { item ->
                            (tags.isEmpty() || tags.all { it in item.tags }) &&
                                (gameModes.isEmpty() || gameModes.all { item.maps[it] == true }) &&
                                (query.isBlank() || item.name.contains(query, ignoreCase = true))
                        }
                        if (matched.isEmpty()) null else name to matched
                    }
                    UiState.Success(ItemListDisplay(filtered))
                }
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)
```

Delete `fun toggleCategoryFilter(category: String) { ... }` (the block right after `updateSearchQuery`). Replace `fun clearFilters() { ... }` with:

```kotlin
    fun clearFilters() {
        savedStateHandle[KEY_SELECTED_TAGS] = emptySet<String>()
        savedStateHandle[KEY_SELECTED_GAME_MODES] = emptySet<String>()
    }
```

- [ ] **Step 4: Update `ItemFilterBottomSheet.kt`**

In `app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt`, remove the `ALL_CATEGORIES` import (line 26). Remove the `selectedCategories: Set<String>` parameter (line 55) and `onCategoryToggle: (String) -> Unit` parameter (line 58) from `ItemFilterBottomSheet`'s signature. Remove the entire Category section (lines 76-93 — the `Text(text = stringResource(R.string.filter_category), ...)` and its following `FlowRow`).

The composable's parameter list becomes:

```kotlin
fun ItemFilterBottomSheet(
    availableTags: List<String>,
    selectedTags: Set<String>,
    selectedGameModes: Set<String>,
    onTagToggle: (String) -> Unit,
    onGameModeToggle: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
```

and the `Column` body starts directly with the existing Game Mode section (currently at lines 95-112), followed by the existing Tags section and Clear-all button, unchanged.

- [ ] **Step 5: Update `ItemFilterBottomSheetTest.kt`**

In `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt`, remove the `selectedCategories = ...` and `onCategoryToggle = ...` lines from all four `ItemFilterBottomSheet(...)` call sites (`tappingTagChip_invokesOnTagToggle`, `tappingClearAll_invokesOnClearAll`, `tappingGameModeChip_invokesOnGameModeToggle`, `multipleGameModeChipsCanBeSelectedSimultaneously`). For example, `tappingTagChip_invokesOnTagToggle`'s call becomes:

```kotlin
                ItemFilterBottomSheet(
                    availableTags = listOf("Boots", "Damage"),
                    selectedTags = emptySet(),
                    selectedGameModes = emptySet(),
                    onTagToggle = { toggledTag = it },
                    onGameModeToggle = {},
                    onClearAll = {},
                    onDismiss = {},
                )
```

Apply the same removal (drop `selectedCategories`/`onCategoryToggle`, keep every other argument as-is) to the other three call sites.

- [ ] **Step 6: Update `ItemScreen.kt`**

In `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`, delete line 106 (`val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()`).

Replace line 125 (`isFilterActive = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || selectedGameModes.isNotEmpty(),`) with:

```kotlin
        isFilterActive = selectedTags.isNotEmpty() || selectedGameModes.isNotEmpty(),
```

In the `ItemFilterBottomSheet(...)` call (lines 134-144), delete the lines `selectedCategories = selectedCategories,` and `onCategoryToggle = viewModel::toggleCategoryFilter,`.

Replace the `is UiState.Success ->` branch inside `ItemScreen`'s `when (itemListState)` (lines 220-262) with:

```kotlin
            is UiState.Success -> {
                val display = itemListState.data
                if (display.groups.isEmpty()) {
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
                }
            }
```

- [ ] **Step 7: Update `ItemScreenTest.kt`**

In `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt`, replace `categorizedDisplay_showsCategoryHeaders`'s body:

```kotlin
                ItemScreen(
                    itemListState = UiState.Success(
                        ItemListDisplay(listOf("Starter" to listOf(longSword)))
                    ),
                    version = "",
                    onItemClick = {},
                )
```

Delete `flatDisplay_showsNoHeaders` entirely (there is no more ungrouped display mode to test).

Replace every remaining `ItemListDisplay.Flat(...)` construction with `ItemListDisplay(...)` (same arguments) in: `filterIconClick_invokesCallback`, `emptyFlatDisplay_showsNoResultsMessage`, `nonEmptyFlatDisplay_doesNotShowNoResultsMessage`, `activeGameModeChip_rendersWhenSelected`, `activeGameModeChip_hiddenWhenNoneSelected`, `activeGameModeChip_clickInvokesOnGameModeClear`, `multipleActiveGameModeChips_renderOneChipEach`, `clickingOneOfMultipleChips_clearsOnlyThatMode`.

Rename `emptyFlatDisplay_showsNoResultsMessage` to `emptyGroups_showsNoResultsMessage` and `nonEmptyFlatDisplay_doesNotShowNoResultsMessage` to `nonEmptyGroups_doesNotShowNoResultsMessage`; for the latter, change `ItemListDisplay(listOf(sorceresShoes))` to `ItemListDisplay(listOf("Starter" to listOf(sorceresShoes)))` since the constructor now takes groups, not a flat item list:

```kotlin
    @Test
    fun nonEmptyGroups_doesNotShowNoResultsMessage() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ItemScreen(
                    itemListState = UiState.Success(ItemListDisplay(listOf("Starter" to listOf(sorceresShoes)))),
                    version = "",
                    onItemClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No items match your filters.").assertDoesNotExist()
    }
```

For every other renamed call site above (the game-mode-chip tests, `filterIconClick_invokesCallback`, `emptyGroups_showsNoResultsMessage`), `ItemListDisplay.Flat(emptyList())` simply becomes `ItemListDisplay(emptyList())` — an empty groups list still renders zero headers/items, same as before.

- [ ] **Step 8: Remove the unreferenced `filter_category` string resource**

Delete the `<string name="filter_category">...</string>` line from each of these 22 files (exact line number shown; content is a localized translation, delete the whole line regardless of language):

| File | Line |
|---|---|
| `app/src/main/res/values/strings.xml` | 59 |
| `app/src/main/res/values-cs/strings.xml` | 32 |
| `app/src/main/res/values-de/strings.xml` | 32 |
| `app/src/main/res/values-el/strings.xml` | 32 |
| `app/src/main/res/values-es/strings.xml` | 32 |
| `app/src/main/res/values-fr/strings.xml` | 32 |
| `app/src/main/res/values-hu/strings.xml` | 32 |
| `app/src/main/res/values-in/strings.xml` | 32 |
| `app/src/main/res/values-it/strings.xml` | 32 |
| `app/src/main/res/values-ja/strings.xml` | 41 |
| `app/src/main/res/values-ko/strings.xml` | 41 |
| `app/src/main/res/values-pl/strings.xml` | 32 |
| `app/src/main/res/values-pt-rBR/strings.xml` | 32 |
| `app/src/main/res/values-ro/strings.xml` | 32 |
| `app/src/main/res/values-ru/strings.xml` | 32 |
| `app/src/main/res/values-th/strings.xml` | 32 |
| `app/src/main/res/values-tr/strings.xml` | 32 |
| `app/src/main/res/values-vi/strings.xml` | 32 |
| `app/src/main/res/values-zh-rCN/strings.xml` | 41 |
| `app/src/main/res/values-zh-rMY/strings.xml` | 32 |
| `app/src/main/res/values-zh-rTW/strings.xml` | 32 |

After deleting, verify no references remain:

Run: `grep -rn "filter_category" app/src/main/res app/src/main/java app/src/test`

Expected: no output.

- [ ] **Step 9: Run the full test suite to verify everything passes**

Run: `./gradlew testDebugUnitTest`

Expected: PASS — all tests, including the rewritten `ItemViewModelTest`, `ItemFilterBottomSheetTest`, and `ItemScreenTest`.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt \
  app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt \
  app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt \
  app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt \
  app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt \
  app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt \
  app/src/main/res/values*/strings.xml
git commit -m "feat: keep category groups while filtering, remove category filter dimension"
```
