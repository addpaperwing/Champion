# Settings as a third bottom-nav tab — Design

## Goal

Move Settings from a screen pushed via a gear icon into a peer of Champions and
Items in the bottom nav bar. Remove the gear icon and the version text line
from the Champions/Items headers. Surface both the app version and the game
(data) version as rows inside Settings instead.

## Current state (for reference)

- `TOP_LEVEL_TABS` (`TopLevelDestinations.kt`) drives both the bottom nav bar
  and the tab-refresh-on-language/data-change logic. It currently lists
  Champions and Items only.
- `Header` (champion index) and `ItemsHeader` (items) each render a gear
  `IconButton` (`onSettingClick`) and a `VersionText` line showing the game
  version (`"v%s"` format).
- `SETTINGS_ROUTE` is a screen *pushed* on top of the current tab (slide-in
  from the right, back arrow pops it). `LANGUAGE_ROUTE` is pushed on top of
  Settings the same way.
- `itemsScreen`'s exit transition is index-aware (slide direction computed
  from the tapped tab's position in `TOP_LEVEL_TABS` relative to Items' own
  position), but its enter transition is hardcoded to always slide in from
  the left. This was harmless with exactly 2 tabs (Items was always the
  rightmost) but breaks once a third tab exists to the right of Items.
- `SettingsRoute`/`SettingScreen.kt` shows two rows today: Switch Language,
  Refresh Data, under a `TopAppBar` with a back arrow.
- `SettingContent.kt` has an unused `Settings` composable (with
  `SelectableBottomMenu` version pickers) that predates the current
  `SettingsRoute` and isn't wired into the app. Out of scope — left as is.

## Changes

### 1. Bottom nav: add Settings as a third tab

- Add `NavTab(SETTINGS_ROUTE, R.drawable.ic_settings, R.string.settings)` to
  `TOP_LEVEL_TABS`, after Items. `refreshesOnDataChange` defaults to `false`
  (Settings has no remotely-fetched content).
- New `app/src/main/res/drawable/ic_settings.xml`: single-path 24dp vector
  (gear glyph), matching the existing flat-black-fill style of
  `ic_champions.xml` / `ic_items.xml` (tinted at runtime by
  `NavigationBarItemDefaults`).

### 2. Navigation graph: Settings becomes a top-level tab

- `SettingsNavigation.kt`: `settingsScreen(...)` transitions change from the
  "push/pop" slide (always Start-direction enter/exit) to the shared
  index-aware tab transition (see below). Drop the `onBack` callback — a
  top-level tab has nothing to pop back to.
- `ChampionNavHost.kt`: the bottom nav's `navigateSingleTopTo` already
  handles tab switching generically via `TOP_LEVEL_TABS`, so no special-case
  wiring is needed for Settings beyond adding it to the list. Remove the
  `onSettingClick = { navController.navigate(SETTINGS_ROUTE) {...} }` calls
  passed into `championIndexScreen(...)` and `itemsScreen(...)`.
- `LanguageNavigation` (pushed from within Settings) is unaffected — it still
  slides in/out and pops back to Settings normally.

### 3. Shared index-aware tab transition helper

Add a small helper (e.g. in `ChampionNavHost.kt` or a new
`TabTransitions.kt`) that, given a route's own index in `TOP_LEVEL_TABS` and
the transition scope's initial/target destination, returns the correct
`SlideDirection` for both enter and exit:

```
private fun tabSlideDirection(ownRoute: String, otherRoute: String?): AnimatedContentTransitionScope.SlideDirection {
    val ownIdx = TOP_LEVEL_TABS.indexOfFirst { it.route == ownRoute }
    val otherIdx = TOP_LEVEL_TABS.indexOfFirst { it.route == otherRoute }
    return if (otherIdx in 0 until ownIdx) SlideDirection.End else SlideDirection.Start
}
```

`championIndexScreen`, `itemsScreen`, and `settingsScreen` all use this for
both `enterTransition` (compare against `initialState.destination.route`) and
`exitTransition` (compare against `targetState.destination.route`), replacing
the existing hardcoded/partially-hardcoded logic. This fixes the Items
enter-transition bug described above and keeps all three tabs consistent.

**Implementation note (found during task review):** the pseudocode above
resolves an unmatched route (`indexOfFirst` returning `-1`) as "further
back than any tab," which is wrong for the two non-tab child screens pushed
from a top-level tab — `CHAMPION_DETAIL_ROUTE` (from Champion Index) and
`LANGUAGE_ROUTE` (from Settings). Both are always reached by a forward push,
so the shipped `TabTransitions.kt` resolves an unmatched route to
`Int.MAX_VALUE` instead of `-1`, treating it as "further forward than any
tab." This keeps the shared helper's computed direction consistent with
`championDetailScreen`'s and `languageScreen`'s own untouched, hardcoded
transitions. See `TabTransitionsTest.kt`'s
`exitDirection_toPushedChildScreen_isStart` /
`enterDirection_fromPushedChildScreen_isEnd` for the regression coverage.

### 4. Remove gear icon + version text from Champions/Items headers

- `ChampionIndexHeader.kt`: remove the `IconButton`/`Icons.Default.Settings`
  and the `VersionText` call from `Header`. Drop the `onSettingClick`
  parameter.
- `ItemScreen.kt`: remove the `IconButton`/gear icon and `VersionText` call
  from `ItemsHeader`. Drop the `onSettingClick` parameter from `ItemsHeader`,
  `ItemScreen`, and `ItemRoute`.
- Cascade the parameter removal through `ChampionIndexScreen`,
  `ChampionIndexRoute`, `ChampionIndexNavigation.championIndexScreen`,
  `ItemsNavigation.itemsScreen`.
- The `version` data itself is **not** removed — `ChampionCard`/`ItemCard`
  still need it for CDN image URLs. Only the visible `VersionText` line goes.
- `VersionText` (`UtilComponents.kt`) becomes unused once both call sites are
  gone — delete it.

### 5. Settings screen restyle

- `SettingContent.kt`: replace `SettingAppbar`'s `TopAppBar` (with back
  arrow) with Material3's `LargeTopAppBar` and no `navigationIcon` — this
  gives the left-aligned, larger-font title per Material Design's large
  app bar pattern directly, no custom layout needed.
- `SettingScreen.kt` (`SettingsRoute`): drop the `onBack` parameter; the
  `SettingAppbar` usage drops `onBack` too.

### 6. New version rows in Settings

- `SettingsViewModel` gains `val gameVersion: StateFlow<String>` sourced from
  `appDataRepository.getLocalVersion()` (`stateIn`, same pattern as
  `currentLanguage`).
- `SettingsRoute` appends two `SettingItem` rows after "Switch Language" and
  "Refresh Data", in this order:
  1. **App Version** — plain `BuildConfig.VERSION_NAME` (e.g. `"1.0"`).
  2. **Game Version** — `stringResource(R.string.v_, gameVersion)` (e.g.
     `"v25.1.1"`), the same `"v%s"` format removed from the headers.
- Game Version row reuses the existing `R.string.latest_game_version`
  ("Latest game version: ") label — already translated in all 18 locales, no
  new translation work.
- App Version row needs one new string, `app_version` (e.g. `"App version: "`,
  matching the trailing-colon style of the other two labels). This one new
  key must be added to all locale files (like the items-page strings round),
  to avoid a repeat `MissingTranslation` lint failure.

## Test impact

- `ChampionIndexScreenshotTest` and any `ChampionIndexScreen`/`ItemScreen`
  previews/tests referencing `onSettingClick` drop that parameter.
- Regenerate the `ChampionIndexScreen` Roborazzi golden (header is shorter:
  no gear icon, no version line).
- `SettingsViewModelTest` gets a case covering `gameVersion`.
- Manual check: tab slide direction looks correct in both directions for all
  three tabs (Champions↔Items, Items↔Settings, Champions↔Settings).

## Out of scope

- The unused `Settings`/`SelectableBottomMenu` composable in
  `SettingContent.kt` — left as dead code, not part of this change.
- Any change to how `version` is used for image URLs.
