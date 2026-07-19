# Item Filter Follow-up: AND Semantics, Multi-Select Game Mode, Arena Fix

## Context

`docs/specs/2026-07-18-item-filter-game-mode-design.md` shipped a single-select
game-mode filter (merged into `feature/item-filter`, commits `9fa1ffa`
`b11fb8d` `a4fea8e`). Manual testing since then surfaced one real defect and
two behavior changes:

- **Arena filter shows zero items — a real bug.** `GAME_MODE_ARENA` is defined
  as `"22"`. Verified against live Data Dragon 16.14.1: map ID `22` is
  Teamfight Tactics' Convergence board, which is `false` on every League item
  (Arena-only items included). Arena's actual Data Dragon map ID is `30`
  (confirmed: an Arena item like Juice of Power has `"30": true, "22": false`;
  a normal Summoner's Rift item has `"22": false` too). Selecting the Arena
  chip today always filters out every item.
- **Tags should combine with AND, not OR.** Today, `item.tags.any { it in
  tags }` — an item shows if it has *any* selected tag. This should require
  *all* selected tags to be present.
- **Game mode should be multi-select, combined with AND.** Today it's
  single-select (`selectedGameMode: String?`, choosing one deselects any
  other). This becomes a `Set<String>` like categories/tags, and an item must
  be available on *every* selected mode (not just any).

Categories are explicitly **not** changing: each item is bucketed into
exactly one category by `categorizeItems()`, so requiring an item to match
*all* selected categories would always yield zero results once 2+ categories
are selected. Categories keep their current OR-within-group behavior
(confirmed with the user). Cross-group combination (category AND tags AND
game modes AND search text) is unchanged — that was already AND and stays
AND.

## Goals

- Fix `GAME_MODE_ARENA` to use the correct map ID (`"30"`).
- Change tag filtering from OR-within to AND-within (item must have every
  selected tag).
- Change game mode from single-select to multi-select, combined with AND
  (item must be available on every selected mode).
- Update the active-game-mode chip row (under the search bar) to show one
  dismissible chip per selected mode instead of a single chip.
- Update the bottom sheet's Game Mode section to allow multiple chips
  selected at once, matching how Category/Tags chips already behave visually.

## Non-goals

- No change to category filtering semantics (stays OR-within).
- No change to how the four filter groups combine with each other (stays
  AND — category AND tags AND game modes AND search).
- No change to `Item`, Room schema, or network layer.
- No change to the curated three-mode list (Summoner's Rift, ARAM, Arena) —
  still fixed, not derived from raw data.

## Data layer

```kotlin
internal const val GAME_MODE_SUMMONERS_RIFT = "11"
internal const val GAME_MODE_ARAM           = "12"
internal const val GAME_MODE_ARENA          = "30"  // was "22" (TFT Convergence, always false)
```

`ALL_GAME_MODES` unchanged (still lists the three constants).

## ViewModel changes (`ItemViewModel.kt`)

```kotlin
private const val KEY_SELECTED_GAME_MODES = "selected_game_modes" // renamed from *_GAME_MODE

val selectedGameModes: StateFlow<Set<String>> =
    savedStateHandle.getStateFlow(KEY_SELECTED_GAME_MODES, emptySet())

fun toggleGameMode(mapId: String) {
    val current = selectedGameModes.value
    savedStateHandle[KEY_SELECTED_GAME_MODES] = if (mapId in current) current - mapId else current + mapId
}
```

`selectGameMode(mapId)` is removed; `toggleGameMode(mapId)` replaces it,
mirroring `toggleCategoryFilter`/`toggleTagFilter` exactly.

`itemListState`'s `combine` keeps 5 inputs, swapping `selectedGameMode` for
`selectedGameModes`. Categorized-vs-Flat decision and predicate:

```kotlin
if (categories.isEmpty() && tags.isEmpty() && gameModes.isEmpty()) {
    // Categorized, unchanged
} else {
    val flat = groups.flatMap { it.second }.filter { item ->
        (categories.isEmpty() || categoryByItemId[item.id] in categories) &&
            (tags.isEmpty() || tags.all { it in item.tags }) &&
            (gameModes.isEmpty() || gameModes.all { item.maps[it] == true }) &&
            (query.isBlank() || item.name.contains(query, ignoreCase = true))
    }
}
```

Category predicate is unchanged (still OR-within, via set membership on the
single assigned category). Tags predicate changes from `.any` to `.all`.
Game mode predicate changes from equality-with-nullable to `.all` over the
set.

`clearFilters()` resets `KEY_SELECTED_GAME_MODES` to `emptySet<String>()`
instead of `null`.

## UI changes

### `ItemFilterBottomSheet.kt`

- `selectedGameMode: String?` param → `selectedGameModes: Set<String>`.
- `onGameModeSelect: (String) -> Unit` → `onGameModeToggle: (String) -> Unit`
  (renamed for clarity; same signature, now backed by `toggleGameMode`).
- Game Mode `FlowRow`: `selected = mode in selectedGameModes` (was `mode ==
  selectedGameMode`), same as Category/Tags chips already do. No other visual
  change — gold styling from the prior feature is untouched.

### `ItemScreen.kt` / `ItemRoute`

- `selectedGameMode: String?` param → `selectedGameModes: Set<String>` on
  `ItemScreen`.
- `isFilterActive` computation: `selectedGameModes.isNotEmpty()` instead of
  `!= null`.
- The single `ActiveGameModeChip` under the search bar becomes a `FlowRow`
  (matching the bottom sheet's existing `FlowRow` pattern) rendering one
  `InputChip` per entry in `selectedGameModes`, each with its own clear
  action (`onGameModeClear(mode)` — becomes a `(String) -> Unit` instead of
  `() -> Unit`). Only rendered when `selectedGameModes.isNotEmpty()`. Visual
  styling (gold, close icon) is unchanged from the existing single-chip
  version.

## Testing

- `ItemViewModelTest`:
  - Replace single-select toggle test with multi-select: toggle A on, toggle
    B on → both active; toggle A off → only B remains.
  - Game-mode-alone filtering with 2 modes selected: item must be available
    on both to match (AND, not OR).
  - Tag-alone filtering with 2 tags selected: item must have both tags to
    match (AND, not OR) — replaces the old any-match assertion.
  - Arena filtering now uses `"30"` in fixtures and assertions (was `"22"`).
  - `clearFilters()` resets `selectedGameModes` to empty set.
- `ItemFilterBottomSheetTest`: tapping two different game mode chips selects
  both (both show `selected = true`); tapping an already-selected chip
  deselects only that one.
- `ItemScreenTest`: multiple selected game modes render multiple chips under
  the search bar; clearing one chip removes only that mode.
- `TestItemRepository.kt`: fixtures using `"22"` to represent Arena
  availability are changed to `"30"`, keeping the same intent (which items
  are/aren't available in Arena) but with the corrected map ID.
