# Item Filter Follow-up: Category-Aware Grouping, Drop Category Filter, Hide Unavailable Items

## Context

`docs/specs/2026-07-19-item-filter-and-semantics-design.md` shipped AND-within
tag/game-mode filtering and multi-select game mode (merged into
`feature/item-filter`, commits `f2255d4` `ea44666` `03e01f8` `1a4d6b7`). Manual
use since then surfaced three changes:

- **Category titles disappear entirely once any filter is active.** Today,
  selecting any category/tag/game-mode filter switches the whole grid from
  the titled `Categorized` display to an untitled `Flat` list
  (`ItemViewModel.kt:126-141`). Category headers should stay visible while
  filtering; a category should only drop out once none of its items survive
  the active filters.
- **The category filter itself should go away.** Category stays as a display
  grouping (headers), but users can no longer filter the grid down to one or
  more categories — that filter dimension and its bottom-sheet chips are
  removed.
- **Items unavailable on every map should never appear.** `Item.maps: Map<String,
  Boolean>` can have zero `true` entries (either genuinely empty, or every
  known map ID `false`) for items that aren't purchasable anywhere in the
  curated game modes. These should be excluded from the item list entirely,
  not just filterable.

## Goals

- Always render the grid grouped by category (drop the `Flat` mode). Search,
  tag, and game-mode filters narrow the items shown within each category
  group; a group with zero matching items after filtering is omitted
  (header included).
- Remove category as a filter dimension: no `selectedCategories` state, no
  category chips in `ItemFilterBottomSheet`, no `onCategoryToggle`.
  `categorizeItems()` and the category headers/labels are unaffected — only
  the ability to *filter by* category goes away.
- Exclude items with `maps.values.none { it }` (no map ID is `true`) from the
  item list at the source, so the exclusion cascades to category grouping,
  the available-tags list, and item-detail component/upgrade chip
  resolution — not just the visible grid.

## Non-goals

- No change to tag/game-mode filter semantics (stays AND-within-group, AND
  across groups, as shipped in the prior spec).
- No change to `categorizeItems()`'s bucketing rules or the curated
  three-mode game-mode list.
- No change to `Item`, Room schema, or network layer — `maps` already exists
  and is populated.
- No new UI for "why is this item hidden" — unavailable items are silently
  excluded, same as how the app already silently excludes items with a blank
  `id` in `categorizeItems()`.

## Data flow changes (`ItemViewModel.kt`)

### Filter unavailable items at the source

```kotlin
private fun Item.isAvailableOnAnyMap() = maps.values.any { it }

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

Because this filters the root `_rawItems` flow, every downstream consumer
inherits the exclusion for free: `_categorizedRawItems` (grouping),
`availableTags` (tag chip list), and `_lastKnownItems` (component/upgrade
chip resolution in the item detail sheet — `getItemById` returns `null` for
a hidden item, and `ItemScreen.kt`'s existing `if (resolved != null)` guard
on `onComponentClick` already no-ops on `null`, so tapping a chip pointing to
a hidden item does nothing rather than crashing).

Side effect worth noting: `categorizeItems()`'s `componentIds` set is
computed from `allItemIds` of the (now pre-filtered) list. If an item's
`components` list references an ID that's since been filtered out, that ID
no longer counts as "used as a component" for category-assignment purposes
(e.g. `CATEGORY_STARTER`'s `item.id !in componentIds` check). This is an
expected consequence of filtering upstream, not a bug to guard against.

### Drop the category filter

Remove: `KEY_SELECTED_CATEGORIES`, `selectedCategories: StateFlow<Set<String>>`,
`toggleCategoryFilter(category: String)`. `clearFilters()` no longer resets a
categories key — only tags and game modes:

```kotlin
fun clearFilters() {
    savedStateHandle[KEY_SELECTED_TAGS] = emptySet<String>()
    savedStateHandle[KEY_SELECTED_GAME_MODES] = emptySet<String>()
}
```

`ALL_CATEGORIES`/`CATEGORY_*` constants and `categorizeItems()` are
unchanged — still needed for grouping and header labels. `ALL_CATEGORIES` is
also still used in `availableTags`'s dedup filter (`it !in ALL_CATEGORIES`),
which is unrelated to the filter-by-category feature being removed.

### Simplify categorization state

`categoryByItemId` existed solely so the category filter predicate could
look up an item's assigned category. With that filter gone, it's dead.
Collapse `CategorizedData` and its "atomic pairing" invariant away entirely:

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

### Always-categorized `itemListState`

`ItemListDisplay` collapses from a sealed interface (`Categorized`/`Flat`)
to a single data class, same name (keeps `ItemViewModel`'s public
`StateFlow<UiState<ItemListDisplay>>` signature and most references stable):

```kotlin
data class ItemListDisplay(val groups: List<Pair<String, List<Item>>>)
```

`itemListState`'s `combine` drops `selectedCategories` as an input (4 inputs
now: categorized state, search query, tags, game modes) and always filters
within each group, omitting groups left with zero items:

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

## UI changes

### `ItemFilterBottomSheet.kt`

- Remove the "Category" `Text` header + `FlowRow` of category chips, the
  `selectedCategories: Set<String>` param, `onCategoryToggle: (String) ->
  Unit` param, and the `ALL_CATEGORIES` import. Game Mode and Tags sections
  are unchanged.

### `ItemScreen.kt` / `ItemRoute`

- Drop `selectedCategories` collection and `onCategoryToggle` wiring from
  `ItemRoute`. `isFilterActive` becomes:
  ```kotlin
  isFilterActive = selectedTags.isNotEmpty() || selectedGameModes.isNotEmpty(),
  ```
- `ItemScreen`'s rendering `when (display) { Categorized -> ...; Flat -> ...
  }` collapses to a single path: always render `display.groups` as
  header-then-items, same as today's `Categorized` branch.
- The "no results" empty state (currently `display is Flat && display.items
  .isEmpty()`) becomes: `display.groups.isEmpty()`.

### Strings

- `filter_category` (and its 21 locale translations) becomes unreferenced
  and is removed from every `values*/strings.xml`.

## Testing

- `ItemViewModelTest`:
  - Remove `toggleCategoryFilter`/category-filter-predicate tests.
  - Add: an item with `maps` all-`false` (or empty) is absent from
    `itemListState`, `availableTags`, and `getItemById(...)` returns `null`
    for it.
  - Add: with a tag or game-mode filter active, a category whose items all
    fail to match is omitted from `itemListState.groups` while other
    categories with surviving items remain, headers intact.
  - Update `clearFilters()` test to only assert tags/game-modes are reset.
- `ItemFilterBottomSheetTest`: remove category-chip rendering/toggle tests;
  remove `selectedCategories`/`onCategoryToggle` from remaining test calls.
- `ItemScreenTest`: update for the unified `ItemListDisplay(groups)` shape;
  verify the empty-state message still appears when filtering empties every
  group; verify `isFilterActive` no longer reacts to category state (no
  longer exists to react to).
- `TestItemRepository.kt`: add a fixture item with `maps` entirely `false`
  to exercise the availability exclusion.
