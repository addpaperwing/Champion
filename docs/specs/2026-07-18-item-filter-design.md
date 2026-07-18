# Item Filter Design

## Problem

The item page (`ItemScreen`) currently supports only text search, filtered
in-memory over items already grouped into fixed categories (Starter, Boots,
Mythic, Legendary, Components, Epic, Other) by `categorizeItems()`. There's no
way to filter by category or by the raw Data Dragon `tags` on each item
(e.g. "Damage", "CriticalStrike", "AttackSpeed", "SpellDamage") — that field
exists on the `Item` model today but isn't surfaced in the UI at all.

## Goals

- Add a filter icon at the end of the item page's search bar.
- Let the user filter the item grid by category (the existing 7 computed
  categories) and by tag (raw `item.tags` values), independently or combined.
- When any filter is active, drop the category section headers and show a
  single flat grid of matching items.
- Filter selections persist across rotation/process death, consistent with
  the existing search query behavior.

## Non-goals

- No changes to the `Item` data model, Room schema, or network layer — `tags`
  already exists and is populated from Data Dragon.
- No localization of raw tag strings (see "Tag display names" below).
- No filtering support added to the champion index screen (out of scope; it
  has no filterable dimensions today).

## Filter semantics

- **Within a group:** OR. Selecting multiple tags (e.g. "Boots" + "AttackSpeed")
  matches items with *either* tag. Same for multiple categories.
- **Across groups:** AND. If both a category and a tag are selected, an item
  must satisfy both.
- **Search text:** ANDed with whatever category/tag filters are active, same
  as it's ANDed with categorization today.
- **No filters selected:** identical to current behavior — grouped-by-category
  grid with headers, search narrows within each group.
- **Any filter selected:** headers disappear; a single flat grid shows every
  item matching the combined predicate above.

## ViewModel changes (`ItemViewModel.kt`)

New persisted filter state, following the existing `searchQuery` /
`KEY_SEARCH_QUERY` `SavedStateHandle` pattern:

```kotlin
private const val KEY_SELECTED_CATEGORIES = "selected_categories"
private const val KEY_SELECTED_TAGS = "selected_tags"

val selectedCategories: StateFlow<Set<String>> =
    savedStateHandle.getStateFlow(KEY_SELECTED_CATEGORIES, emptySet())
val selectedTags: StateFlow<Set<String>> =
    savedStateHandle.getStateFlow(KEY_SELECTED_TAGS, emptySet())

fun toggleCategoryFilter(category: String) { /* XOR category in/out of the saved set */ }
fun toggleTagFilter(tag: String) { /* XOR tag in/out of the saved set */ }
fun clearFilters() { /* reset both sets to emptySet() */ }
```

`availableTags: StateFlow<List<String>>` — distinct, sorted tags derived from
`_rawItems` (dynamic; always reflects whatever's actually loaded, no manual
upkeep). Available categories are the existing fixed compile-time list of 7
(`CATEGORY_STARTER` … `CATEGORY_OTHER`) — always shown regardless of what's
currently loaded, since they're a fixed enum, not data-dependent.

Replace the current `categorizedItems: StateFlow<UiState<List<Pair<String,
List<Item>>>>>` with a single display-mode state:

```kotlin
sealed interface ItemListDisplay {
    data class Categorized(val groups: List<Pair<String, List<Item>>>) : ItemListDisplay
    data class Flat(val items: List<Item>) : ItemListDisplay
}

val itemListState: StateFlow<UiState<ItemListDisplay>>
```

Computed via `combine(_categorizedRawItems, searchQuery, selectedCategories,
selectedTags)`:

- `Loading` / `Error` pass through unchanged.
- `Success` with both filter sets empty → apply today's per-category text
  filter, wrap as `Categorized` (unchanged existing behavior).
- `Success` with either filter set non-empty → flatten all category groups
  into a single item list (retaining each item's assigned category from
  `_categorizedRawItems`'s grouping for the category-match check), apply the
  combined predicate described above, wrap as `Flat`.

## SearchBar extensibility (`ui/index/compose/SearchBar.kt`)

`SearchTextField` is shared by the champion index screen, the item screen,
and `PredictionSearchBar`. Add one new optional parameter:

```kotlin
@Composable
fun SearchTextField(
    modifier: Modifier = SearchFieldDefaultModifier,
    text: String,
    onTextChanged: (String) -> Unit,
    onClearText: (() -> Unit)? = null,
    onDone: (String) -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
)
```

Rendered inside the existing `trailingIcon` slot, after the clear icon.
Defaults to `null` — champion search and `PredictionSearchBar` pass nothing
and render exactly as today; only the item screen supplies it.

## Filter icon & bottom sheet (new file `ui/items/compose/ItemFilterBottomSheet.kt`)

- Filter icon (`Icons.Filled.FilterList` / `Icons.Outlined.FilterList` when
  inactive) passed as `trailingContent` from `ItemScreen`. Tint is
  `MaterialTheme.colorScheme.tertiary`/default when no filters are active,
  and switches to `Golden` (the existing accent color used for category
  headers) when `selectedCategories` or `selectedTags` is non-empty — reusing
  the app's existing "Golden = active/highlighted" convention rather than
  introducing a new badge treatment.
- Tapping opens a `ModalBottomSheet`, matching the existing `ItemBottomSheet`
  pattern (dismiss via scrim tap / swipe / back).
- Two sections, each a `FlowRow` of `FilterChip`s:
  - **Category:** the 7 fixed categories, localized via the existing
    `categoryNameResIds` map (same lookup already used by `CategoryHeader`).
  - **Tags:** `availableTags` from the ViewModel, displayed as raw strings
    (see "Tag display names" below).
- Chips toggle live — selecting/deselecting calls `toggleCategoryFilter` /
  `toggleTagFilter` directly, immediately updating the grid behind the sheet.
  No Apply/Cancel staging.
- A "Clear all" text action calls `clearFilters()`.

## Tag display names

Category names are localized today because they're a fixed, known-at-compile-time
enum (7 values, all mapped in `strings.xml` across all 27 languages). Tags are
not: they're an open-ended set of raw strings from the Data Dragon API, and
Riot does not localize them (the `tags` array is identical across all Data
Dragon locale endpoints). Pre-mapping and translating an unbounded, API-owned
vocabulary is out of scope here. **Decision: tag chips display the raw
Data Dragon string as-is (e.g. "CriticalStrike", "SpellDamage") in all
locales.** This can be revisited later with a curated string-resource mapping
if it becomes a real pain point.

## Wiring (`ItemScreen.kt` / `ItemRoute`)

- `ItemRoute` collects `itemListState`, `selectedCategories`, `selectedTags`,
  and `availableTags`, and passes toggle/clear callbacks down to `ItemScreen`.
- `ItemScreen`'s existing `when (categorizedState)` becomes `when
  (itemListState)`; the `UiState.Success` branch further dispatches on
  `ItemListDisplay`:
  - `Categorized` → today's rendering (per-group `CategoryHeader` + items),
    unchanged.
  - `Flat` → the same `LazyVerticalGrid`, but a single `items(...)` block
    with no header items.

## Testing

- `ItemViewModelTest`: category-only filter, tag-only filter, combined
  category+tag (AND across groups, OR within), multiple tags/categories (OR
  within group), `clearFilters()`, and search text combined with an active
  filter.
- Compose/screenshot test (if the project's existing Roborazzi setup covers
  `ItemScreen`) for the flat-grid-no-headers rendering when a filter is
  active, alongside the existing categorized rendering.
