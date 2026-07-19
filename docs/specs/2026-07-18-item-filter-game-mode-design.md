# Item Filter Follow-up: Game Mode Filter + Bottom Sheet Polish

## Context

The category/tag filter feature (`docs/specs/2026-07-18-item-filter-design.md`,
`docs/specs/2026-07-18-item-filter-plan.md`) is implemented and merged into
`feature/item-filter`. Manual testing on-device surfaced two real defects and
one new feature request:

- **`filter_clear_all` TextButton is effectively invisible.** `TextButton`
  uses Material3's default content color, which renders as low-contrast
  dark-gray-on-near-black in this app's custom dark theme. Root cause, not a
  perception issue — confirmed by reading `ItemFilterBottomSheet.kt`'s
  `TextButton(onClick = onClearAll)` call, which sets no explicit color.
- **The bottom sheet doesn't lean into the app's existing gold accent
  convention** (`Golden = Color(0xffc28f2c)`, already used for category
  headers and the active filter icon tint) as much as it should.
- **New feature: filter items by game mode.** `Item.maps: Map<String, Boolean>`
  (keyed by Data Dragon numeric map ID, e.g. `"11"` = Summoner's Rift,
  `"12"` = Howling Abyss/ARAM) already exists on the model and is persisted,
  but nothing in the app reads it today — a prior repository-level SR/ARAM
  filter was removed in DB migration 4→5 (`PersistenceModule.kt`) in favor of
  showing all purchasable items unfiltered. This adds it back as an explicit,
  user-controlled filter instead of a silent restriction.

## Goals

- Add a "Game Mode" section to the existing `ItemFilterBottomSheet`, between
  Category and Tags, offering a curated, fixed set of modes (not derived from
  raw data — Data Dragon's `maps` field includes long-retired map IDs that
  should never surface as chips).
- Game mode selection is **single-select**: choosing a mode deselects any
  previous one; tapping the active chip again clears it. This differs
  deliberately from Category/Tags (multi-select, OR-within) because shopping
  is naturally scoped to one mode at a time.
- When a game mode is active, show a small dismissible chip for it directly
  under the search bar (outside the bottom sheet) so the active filter stays
  visible without reopening the sheet.
- Game mode ANDs with any active category/tag/search filters, consistent with
  how those three already combine.
- Fix the `Clear all` contrast bug and refresh the bottom sheet's accent
  color usage toward gold, in the same change set since both touch
  `ItemFilterBottomSheet.kt`.

## Non-goals

- No dynamic/derived game mode list — the three curated modes
  (Summoner's Rift, ARAM, Arena) are the only ones offered, regardless of
  what `Item.maps` actually contains for a given item set.
- No multi-select for game mode.
- No changes to `Item`, Room schema, or network layer — `maps` already
  exists and is populated.
- No broader theme/color system rework — only `ItemFilterBottomSheet.kt`'s
  own accent usage and the `Clear all` button change.

## Data layer

Curated map-ID → display-name-resource list, mirroring the existing
`ALL_CATEGORIES` / `categoryNameResIds` pattern in `ItemViewModel.kt` /
`ItemScreen.kt`:

```kotlin
internal const val GAME_MODE_SUMMONERS_RIFT = "11"
internal const val GAME_MODE_ARAM           = "12"
internal const val GAME_MODE_ARENA          = "22"

internal val ALL_GAME_MODES = listOf(
    GAME_MODE_SUMMONERS_RIFT,
    GAME_MODE_ARAM,
    GAME_MODE_ARENA,
)
```

`gameModeNameResIds` (analogous to `categoryNameResIds`, defined in
`ItemScreen.kt` alongside it) maps each ID to a new string resource:
`R.string.game_mode_summoners_rift`, `R.string.game_mode_aram`,
`R.string.game_mode_arena`.

## ViewModel changes (`ItemViewModel.kt`)

```kotlin
private const val KEY_SELECTED_GAME_MODE = "selected_game_mode"

val selectedGameMode: StateFlow<String?> =
    savedStateHandle.getStateFlow(KEY_SELECTED_GAME_MODE, null)

fun selectGameMode(mapId: String) {
    savedStateHandle[KEY_SELECTED_GAME_MODE] =
        if (savedStateHandle.get<String?>(KEY_SELECTED_GAME_MODE) == mapId) null else mapId
}
```

`itemListState`'s `combine` gains `selectedGameMode` as a 5th input.
Categorized-vs-Flat decision:

```kotlin
if (categories.isEmpty() && tags.isEmpty() && gameMode == null) {
    // Categorized, unchanged
} else {
    // Flat — predicate gains:
    (gameMode == null || item.maps[gameMode] == true)
}
```

`clearFilters()` additionally resets `KEY_SELECTED_GAME_MODE` to `null`.

## UI changes

### `ItemFilterBottomSheet.kt`

- New "Game Mode" `FlowRow` section between Category and Tags, using
  `FilterChip` exactly like the other two sections. `selected = mode ==
  selectedGameMode`; `onClick = { onGameModeSelect(mode) }` (toggles via the
  same select-again-to-clear semantics as the ViewModel method).
- `FilterIconButton`'s `isActive` — driven from `ItemScreen`/`ItemRoute` —
  extends to include `selectedGameMode != null`, alongside the existing
  category/tag check.
- `TextButton(onClick = onClearAll)` gets an explicit
  `colors = ButtonDefaults.textButtonColors(contentColor = Golden)` so it's
  legible against the sheet's dark background (currently unset, inheriting
  Material3's low-contrast default content color — this is the confirmed
  root cause of the reported invisibility).
- All three `FilterChip` calls (Category, the new Game Mode, Tags) get an
  explicit `colors = FilterChipDefaults.filterChipColors(selectedContainerColor
  = Golden.copy(alpha = 0.25f), selectedLabelColor = Golden)`, replacing the
  current unset Material3 default (a muted `secondaryContainer` purple-gray
  that reads as barely different from the unselected state). This reuses the
  exact `Golden.copy(alpha = 0.25f)` value `ItemScreen.kt`'s
  `categoryHeaderBrush` already uses for category headers, so selected chips
  and category headers share one consistent "selected/active" gold
  treatment instead of introducing a new one.

### `ItemScreen.kt` / `ItemRoute`

- `ItemRoute` collects `selectedGameMode`, passes it to `ItemFilterBottomSheet`
  (`onGameModeSelect = viewModel::selectGameMode`), and includes it in the
  `isFilterActive` computation passed to `ItemScreen`.
- New composable rendered in `ItemScreen`'s `Column`, directly below
  `SearchTextField` and above the grid, visible only when
  `selectedGameMode != null`: a single `FilterChip`-styled or similar chip
  showing the mode's localized name with a trailing clear icon, calling back
  up to clear just the game mode (`onGameModeClear`). This is a sibling in
  the `Column`, not part of `SearchTextField`'s `trailingContent` (that slot
  stays exactly the filter icon, unchanged from the existing feature).

## Testing

- `ItemViewModelTest`: single-select toggle (select A, select B → only B
  active; select A, select A again → cleared), game-mode-alone filtering,
  game-mode combined with category/tag (AND across), combined with search
  text, `clearFilters()` resets game mode too.
- `ItemFilterBottomSheetTest`: tapping a game mode chip invokes the callback;
  tapping the already-selected chip invokes it with the clearing semantics
  (exact assertion shape depends on whether `onGameModeSelect` takes the raw
  ID and the ViewModel owns toggle logic, mirroring how `onCategoryToggle`/
  `onTagToggle` already work today).
- `ItemScreenTest`: active game mode chip renders under the search bar when
  `selectedGameMode` (passed to `ItemScreen`) is non-null, and does not
  render when null.

## Localization

Four new strings (`filter_game_mode`, `game_mode_summoners_rift`,
`game_mode_aram`, `game_mode_arena`) added to all 21 `values*/strings.xml`
files, following the exact process used for the original feature's four
strings (insert at a consistent anchor point, translate per-locale, verify
with `:app:processDebugResources`).
