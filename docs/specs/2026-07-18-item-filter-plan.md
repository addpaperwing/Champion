# Item Filter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add category + tag filtering to the item page, surfaced via a filter icon at the end of the search bar, with a bottom sheet of live-toggling chips.

**Architecture:** `ItemViewModel` gains two `SavedStateHandle`-backed filter sets and a single `itemListState: StateFlow<UiState<ItemListDisplay>>` that replaces the old `categorizedItems` — it emits `Categorized` (today's grouped rendering) when no filter is active, or `Flat` (single ungrouped grid) when any category/tag filter is selected. `SearchTextField` gets one new optional `trailingContent` slot so the item screen can inject a filter icon without touching the champion search bar. A new `ItemFilterBottomSheet` renders category/tag `FilterChip`s that call the ViewModel directly (live update, no staging).

**Tech Stack:** Kotlin 2.1.20, Jetpack Compose (BOM 2025.06.00, Foundation 1.8.3 — `FlowRow` available, needs `@OptIn(ExperimentalLayoutApi::class)`), Hilt, Robolectric + `createAndroidComposeRule` for Compose unit tests (existing pattern, see `ChampionIndexScreenshotTest.kt`), MockK, JUnit4.

## Global Constraints

- minSdk 24, targetSdk 36 (per `app/build.gradle.kts`) — no compat-shim APIs needed for this feature.
- No new third-party icon dependency: this project has no `material-icons-extended` dependency and uses hand-added vector drawables for icons outside the small `Icons.Default` core set (see `ic_settings.xml`). Add `ic_filter_list.xml` the same way rather than pulling in `material-icons-extended`.
- New user-facing strings must be added to **all 21** `values*/strings.xml` files (default + 20 locales: cs, de, el, es, fr, hu, in, it, ja, ko, pl, pt-rBR, ro, ru, th, tr, vi, zh-rCN, zh-rMY, zh-rTW) — this project maintains full locale parity for every string (see the `app_version` addition in commit `f63bb5f`). Raw Data Dragon tag values themselves are the one explicit exception (see design doc's "Tag display names" section) — they are not translated.
- Persist filter selections via `SavedStateHandle`, matching the existing `searchQuery`/`KEY_SEARCH_QUERY` pattern in `ItemViewModel.kt`.
- Any new "is this open" UI state (e.g. bottom sheet visibility) must use `rememberSaveable`, not `remember` — this codebase previously shipped a rotation bug from using plain `remember` for UI state that should have survived rotation (`LanguageScreen`), so follow the established fix pattern here from the start.
- Compose UI tests for this feature go in `app/src/test` using `createAndroidComposeRule<ComponentActivity>()` + `@RunWith(AndroidJUnit4::class)`, matching the existing Robolectric setup (`testOptions.unitTests.isIncludeAndroidResources = true` is already configured) — do not add new `androidTest` instrumented tests for this feature.

---

### Task 1: Filter state and combined display mode in `ItemViewModel`

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`
- Test: `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`

**Interfaces:**
- Consumes: `TestItemRepository`, `longSword`/`infinityEdge`/`sorceresShoes` fixtures (`app/src/test/java/com/zzy/champions/TestItemRepository.kt`, package `com.zzy.champions`) — unchanged, already exist.
- Produces (for Tasks 3 & 4):
  - `internal sealed interface ItemListDisplay { data class Categorized(val groups: List<Pair<String, List<Item>>>); data class Flat(val items: List<Item>) }`
  - `internal val ALL_CATEGORIES: List<String>` (the 7 `CATEGORY_*` constants, in display order)
  - `ItemViewModel.itemListState: StateFlow<UiState<ItemListDisplay>>` (replaces `categorizedItems`, which is deleted)
  - `ItemViewModel.selectedCategories: StateFlow<Set<String>>`, `selectedTags: StateFlow<Set<String>>`, `availableTags: StateFlow<List<String>>`
  - `ItemViewModel.toggleCategoryFilter(category: String)`, `toggleTagFilter(tag: String)`, `clearFilters()`

- [ ] **Step 1: Write the failing/updated tests**

Replace the full contents of `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`:

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
import com.zzy.champions.sorceresShoes
import com.zzy.champions.ui.items.CATEGORY_BOOTS
import com.zzy.champions.ui.items.CATEGORY_LEGENDARY
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

    @Test
    fun stateIsInitiallyLoading() {
        assertEquals(UiState.Loading, viewModel.itemListState.value)
    }

    @Test
    fun items_loadsSuccessfully() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        val state = viewModel.itemListState.value
        assertTrue(state is UiState.Success)
        val display = (state as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        val totalItems = (display as ItemListDisplay.Categorized).groups.sumOf { it.second.size }
        assertEquals(3, totalItems)
        job.cancel()
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
    fun categoryFilter_showsFlatDisplayWithMatchingItems() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleCategoryFilter(CATEGORY_BOOTS)
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun tagFilter_showsFlatDisplayWithMatchingItems() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(longSword, infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun multipleTagsSelected_matchEitherTag() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Boots")
        viewModel.toggleTagFilter("CriticalStrike")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes, infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun categoryAndTagCombined_mustMatchBoth() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleCategoryFilter(CATEGORY_LEGENDARY)
        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun clearFilters_returnsToCategorizedDisplay() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleCategoryFilter(CATEGORY_BOOTS)
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        job.cancel()
    }

    @Test
    fun searchText_narrowsActiveTagFilter() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        viewModel.updateSearchQuery("Infinity")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun availableTags_derivedFromLoadedItems() = runTest {
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        assertEquals(
            listOf("Boots", "CriticalStrike", "Damage", "Legendary", "SpellDamage"),
            viewModel.availableTags.value,
        )
        job.cancel()
    }
}
```

- [ ] **Step 2: Run tests to verify they fail (compile error)**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`
Expected: FAIL — compile error, `itemListState`/`ItemListDisplay`/`toggleCategoryFilter`/`toggleTagFilter`/`clearFilters`/`availableTags` are unresolved references on `ItemViewModel`.

- [ ] **Step 3: Implement the ViewModel changes**

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

internal sealed interface ItemListDisplay {
    data class Categorized(val groups: List<Pair<String, List<Item>>>) : ItemListDisplay
    data class Flat(val items: List<Item>) : ItemListDisplay
}

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

    // Categorization is computed once from raw items, not re-run on every search keystroke.
    private val _categorizedRawItems: StateFlow<UiState<List<Pair<String, List<Item>>>>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> UiState.Success(categorizeItems(state.data))
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    val selectedCategories: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_CATEGORIES, emptySet())
    val selectedTags: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_TAGS, emptySet())

    val availableTags: StateFlow<List<String>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.flatMap { it.tags }.distinct().sorted()
                else -> emptyList()
            }
        }
        .stateInViewModel(viewModelScope, initialValue = emptyList())

    val itemListState: StateFlow<UiState<ItemListDisplay>> =
        combine(_categorizedRawItems, searchQuery, selectedCategories, selectedTags) { state, query, categories, tags ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    if (categories.isEmpty() && tags.isEmpty()) {
                        val filtered = if (query.isBlank()) state.data
                        else state.data.mapNotNull { (name, items) ->
                            val matched = items.filter { it.name.contains(query, ignoreCase = true) }
                            if (matched.isEmpty()) null else name to matched
                        }
                        UiState.Success(ItemListDisplay.Categorized(filtered))
                    } else {
                        val categoryByItemId = state.data
                            .flatMap { (name, items) -> items.map { it.id to name } }
                            .toMap()
                        val flat = state.data.flatMap { it.second }.filter { item ->
                            (categories.isEmpty() || categoryByItemId[item.id] in categories) &&
                                (tags.isEmpty() || item.tags.any { it in tags }) &&
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

    fun clearFilters() {
        savedStateHandle[KEY_SELECTED_CATEGORIES] = emptySet<String>()
        savedStateHandle[KEY_SELECTED_TAGS] = emptySet<String>()
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

- [ ] **Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"`
Expected: PASS (13 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt
git commit -m "feat: add category/tag filter state to ItemViewModel"
```

---

### Task 2: `trailingContent` slot on `SearchTextField`

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/index/compose/SearchBar.kt:53-111`
- Test: `app/src/test/java/com/zzy/champions/index/SearchBarTest.kt` (new)

**Interfaces:**
- Consumes: nothing new.
- Produces (for Task 4): `SearchTextField(..., trailingContent: (@Composable () -> Unit)? = null)` — renders after the existing clear icon inside the same trailing slot; defaults to `null` so the champion search bar and `PredictionSearchBar` are unaffected.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/zzy/champions/index/SearchBarTest.kt`:

```kotlin
package com.zzy.champions.index

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.ui.index.compose.SearchTextField
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun trailingContent_rendersAlongsideClearIcon() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SearchTextField(
                    text = "boots",
                    onTextChanged = {},
                    onClearText = {},
                    onDone = {},
                    trailingContent = { Text("FILTER_MARKER") },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear").assertExists()
        composeTestRule.onNodeWithText("FILTER_MARKER").assertExists()
    }

    @Test
    fun noTrailingContent_rendersOnlyClearIcon() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SearchTextField(
                    text = "boots",
                    onTextChanged = {},
                    onClearText = {},
                    onDone = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear").assertExists()
        composeTestRule.onNodeWithText("FILTER_MARKER").assertDoesNotExist()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.index.SearchBarTest"`
Expected: FAIL — compile error, `trailingContent` is not a parameter of `SearchTextField`.

- [ ] **Step 3: Add the parameter**

In `app/src/main/java/com/zzy/champions/ui/index/compose/SearchBar.kt`, change the function signature (around line 53-60):

```kotlin
@Composable
fun SearchTextField(
    modifier: Modifier = SearchFieldDefaultModifier,
    text: String,
    onTextChanged: (String) -> Unit,
    onClearText: (() -> Unit)? = null,
    onDone: (String) -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
```

And change the `trailingIcon` block (around line 78-84):

```kotlin
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onClearText != null && text.isNotBlank()) {
                        IconButton(onClick = onClearText) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    trailingContent?.invoke()
                }
            },
```

`Row` and `Alignment` are already imported in this file (used elsewhere in `PredictionItem`), so no new imports are needed.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.index.SearchBarTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/index/compose/SearchBar.kt app/src/test/java/com/zzy/champions/index/SearchBarTest.kt
git commit -m "feat: add optional trailingContent slot to SearchTextField"
```

---

### Task 3: `ItemFilterBottomSheet` + filter icon button + strings/drawable

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt`
- Create: `app/src/main/res/drawable/ic_filter_list.xml`
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt:55` (drop `private` from `categoryNameResIds`)
- Modify: `app/src/main/res/values/strings.xml` and the 20 locale variants (see full list below)
- Test: `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt` (new)

**Interfaces:**
- Consumes: `ALL_CATEGORIES`, `ItemListDisplay` (Task 1); `categoryNameResIds` (from `ItemScreen.kt`, made non-private in this task).
- Produces (for Task 4):
  - `FilterIconButton(isActive: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)`
  - `ItemFilterBottomSheet(availableTags: List<String>, selectedCategories: Set<String>, selectedTags: Set<String>, onCategoryToggle: (String) -> Unit, onTagToggle: (String) -> Unit, onClearAll: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier)`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt`:

```kotlin
package com.zzy.champions.items

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    @Test
    fun tappingTagChip_invokesOnTagToggle() {
        var toggledTag: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                ItemFilterBottomSheet(
                    availableTags = listOf("Boots", "Damage"),
                    selectedCategories = emptySet(),
                    selectedTags = emptySet(),
                    onCategoryToggle = {},
                    onTagToggle = { toggledTag = it },
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
            MyApplicationTheme {
                ItemFilterBottomSheet(
                    availableTags = listOf("Boots"),
                    selectedCategories = setOf("Boots"),
                    selectedTags = emptySet(),
                    onCategoryToggle = {},
                    onTagToggle = {},
                    onClearAll = { cleared = true },
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clear all").performClick()

        assertTrue(cleared)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemFilterBottomSheetTest"`
Expected: FAIL — compile error, `ItemFilterBottomSheet` is unresolved.

- [ ] **Step 3a: Add the drawable**

Create `app/src/main/res/drawable/ic_filter_list.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M10,18h4v-2h-4V18zM3,6v2h18V6H3zM6,13h12v-2H6V13z"/>
</vector>
```

- [ ] **Step 3b: Add the strings (all 21 locale files)**

In every one of the following files, insert the 4 new lines immediately after the existing `<string name="edit">...</string>` line: `app/src/main/res/values/strings.xml`, `values-cs`, `values-de`, `values-el`, `values-es`, `values-fr`, `values-hu`, `values-in`, `values-it`, `values-ja`, `values-ko`, `values-pl`, `values-pt-rBR`, `values-ro`, `values-ru`, `values-th`, `values-tr`, `values-vi`, `values-zh-rCN`, `values-zh-rMY`, `values-zh-rTW` (each at path `app/src/main/res/<dir>/strings.xml`).

Use this table (default English row first, then each locale's translation — insert exactly the 4 lines for that file's language):

| Locale dir | filter_items | filter_category | filter_tags | filter_clear_all |
|---|---|---|---|---|
| values | Filter items | Category | Tags | Clear all |
| values-cs | Filtrovat položky | Kategorie | Značky | Vymazat vše |
| values-de | Gegenstände filtern | Kategorie | Tags | Alle löschen |
| values-el | Φιλτράρισμα αντικειμένων | Κατηγορία | Ετικέτες | Απαλοιφή όλων |
| values-es | Filtrar objetos | Categoría | Etiquetas | Borrar todo |
| values-fr | Filtrer les objets | Catégorie | Tags | Tout effacer |
| values-hu | Tárgyak szűrése | Kategória | Címkék | Összes törlése |
| values-in | Filter item | Kategori | Tag | Hapus semua |
| values-it | Filtra oggetti | Categoria | Tag | Cancella tutto |
| values-ja | アイテムをフィルター | カテゴリー | タグ | すべてクリア |
| values-ko | 아이템 필터 | 카테고리 | 태그 | 모두 지우기 |
| values-pl | Filtruj przedmioty | Kategoria | Tagi | Wyczyść wszystko |
| values-pt-rBR | Filtrar itens | Categoria | Tags | Limpar tudo |
| values-ro | Filtrează obiectele | Categorie | Etichete | Șterge tot |
| values-ru | Фильтр предметов | Категория | Теги | Очистить всё |
| values-th | กรองไอเทม | หมวดหมู่ | แท็ก | ล้างทั้งหมด |
| values-tr | Eşyaları filtrele | Kategori | Etiketler | Tümünü temizle |
| values-vi | Lọc vật phẩm | Danh mục | Thẻ | Xóa tất cả |
| values-zh-rCN | 筛选物品 | 分类 | 标签 | 全部清除 |
| values-zh-rMY | 筛选物品 | 分类 | 标签 | 全部清除 |
| values-zh-rTW | 篩選物品 | 分類 | 標籤 | 全部清除 |

For each file, the inserted block (substituting that row's values) is:

```xml
    <string name="filter_items">Filter items</string>
    <string name="filter_category">Category</string>
    <string name="filter_tags">Tags</string>
    <string name="filter_clear_all">Clear all</string>
```

(shown here with the English values; use the corresponding row from the table above for every other locale file).

- [ ] **Step 3c: Make `categoryNameResIds` non-private**

In `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`, change:

```kotlin
private val categoryNameResIds = mapOf(
```

to:

```kotlin
internal val categoryNameResIds = mapOf(
```

- [ ] **Step 3d: Create the composable**

Create `app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt`:

```kotlin
package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemFilterBottomSheet(
    availableTags: List<String>,
    selectedCategories: Set<String>,
    selectedTags: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit,
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
                    )
                }
            }

            TextButton(onClick = onClearAll) {
                Text(stringResource(R.string.filter_clear_all))
            }
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemFilterBottomSheetTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Verify the locale XML is well-formed**

Run: `.\gradlew.bat :app:processDebugResources`
Expected: BUILD SUCCESSFUL (fails with an AAPT XML-parse error if any of the 21 files was edited incorrectly).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemFilterBottomSheet.kt app/src/main/res/drawable/ic_filter_list.xml app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt app/src/main/res/values*/strings.xml app/src/test/java/com/zzy/champions/items/ItemFilterBottomSheetTest.kt
git commit -m "feat: add item filter bottom sheet with category/tag chips"
```

---

### Task 4: Wire filtering into `ItemScreen`/`ItemRoute`

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`
- Test: `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt` (new)

**Interfaces:**
- Consumes: `ItemListDisplay`, `itemListState`, `selectedCategories`, `selectedTags`, `availableTags`, `toggleCategoryFilter`, `toggleTagFilter`, `clearFilters` (Task 1); `FilterIconButton`, `ItemFilterBottomSheet` (Task 3); `trailingContent` param on `SearchTextField` (Task 2).
- Produces: nothing further consumed by other tasks — this is the final integration point.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt`:

```kotlin
package com.zzy.champions.items

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
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

@RunWith(AndroidJUnit4::class)
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemScreenTest"`
Expected: FAIL — compile error, `ItemScreen`'s `categorizedState` parameter doesn't accept `UiState<ItemListDisplay>`, and `isFilterActive`/`onFilterIconClick` don't exist yet.

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
        isFilterActive = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty(),
        onFilterIconClick = { showFilterSheet = true },
        onItemClick = viewModel::selectItem,
        onReloadClick = viewModel::retry,
    )

    if (showFilterSheet) {
        ItemFilterBottomSheet(
            availableTags = availableTags,
            selectedCategories = selectedCategories,
            selectedTags = selectedTags,
            onCategoryToggle = viewModel::toggleCategoryFilter,
            onTagToggle = viewModel::toggleTagFilter,
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLUMNS),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    when (val display = itemListState.data) {
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

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.zzy.champions.items.ItemScreenTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Run the full unit test suite**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: BUILD SUCCESSFUL — no regressions in `ItemCategorizationTest`, `ItemViewModelTest`, `SearchBarTest`, `ItemFilterBottomSheetTest`, `ItemScreenTest`, or any other existing suite (particularly `ChampionIndexScreenshotTest`/`ChampionViewModelTest`, since `SearchTextField` is shared).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt app/src/test/java/com/zzy/champions/items/ItemScreenTest.kt
git commit -m "feat: wire category/tag filtering into ItemScreen"
```

---

## Self-Review Notes

- **Spec coverage:** filter icon in search bar (Task 2+4), category+tag filter chips (Task 3), OR-within/AND-across semantics (Task 1, tested), headers-disappear-when-filtered (Task 4, tested), persistence via `SavedStateHandle` (Task 1), dynamic tag list (Task 1's `availableTags`, tested), active-state icon tint (Task 3/4), raw untranslated tag strings (Task 3, explicit design decision carried through) — all spec sections have a corresponding task.
- **Placeholder scan:** no TBD/TODO; every step has runnable code and exact commands.
- **Type consistency:** `ItemListDisplay`, `itemListState`, `toggleCategoryFilter`/`toggleTagFilter`/`clearFilters`, `availableTags`, `ALL_CATEGORIES`, `FilterIconButton`, `ItemFilterBottomSheet` are named and typed identically everywhere they're defined (Task 1/3) and consumed (Task 3/4).
- **Deviation from design doc:** the design doc's "Filter icon & bottom sheet" section mentions `Icons.Filled.FilterList`/`Icons.Outlined.FilterList` (Material icons). This project has no `material-icons-extended` dependency and `FilterList` isn't in the core `Icons.Default` set, so Task 3 uses a hand-added vector drawable instead (matching the existing `ic_settings.xml` precedent) with a single glyph whose tint toggles between default and `Golden` — this fully satisfies the design's actual behavioral requirement (tint changes when active) without adding a new dependency.
