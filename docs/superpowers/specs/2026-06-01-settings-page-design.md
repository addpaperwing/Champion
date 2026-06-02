# Settings Page — Design Spec
_Date: 2026-06-01_

## Overview

Add a settings entry point (gear icon) to the champion index screen and implement a settings page with two actions: switch language and refresh local data.

---

## Screens

### 1. Settings screen
- Accessed via a gear `IconButton` at the top-right of the existing `Header` composable (already stubbed, just commented out).
- `TopAppBar` with back arrow + title "Settings".
- Two rows using the existing `SettingItem` composable pattern:
  - **Switch Language** — trailing chevron, navigates to the language picker screen.
  - **Refresh Data** — trailing refresh icon, triggers a confirmation dialog inline.

### 2. Language picker screen
- `TopAppBar` with back arrow + title "Select Language".
- `LazyColumn` of all languages returned by `api.getSupportLanguage()`.
- Current language is highlighted with a checkmark.
- Tapping a row opens a confirmation dialog: _"Champion data will be reloaded in [lang]. Continue?"_ → Cancel / Confirm.
- On confirm: save language, clear local data, reset use case cache, set refresh signal on index back-stack entry, pop entire settings stack back to index.
- Loading and error states shown while languages fetch.

### 3. Confirmation dialogs
- Material3 `AlertDialog` (not the existing custom `TextDialog`).
- **Language switch:** "Switch Language?" / "Champion data will be reloaded in [lang]." / Cancel + Confirm.
- **Refresh data:** "Refresh Data?" / "All local data will be cleared and reloaded from the internet." / Cancel + Confirm.

---

## Navigation

Routes added to `ChampionNavHost`:
- `settings` — slide in from right, slide out to right.
- `settings/language` — slide in from right, slide out to right.

The currently commented-out `onSettingClick` in `ChampionNavHost` is wired to `navController.navigate("settings")`.

**Returning to index with a reload:** settings sets `"refresh" = true` on `navController.previousBackStackEntry?.savedStateHandle` before popping. `ChampionIndexRoute` reads this via `navController.currentBackStackEntry?.savedStateHandle?.getStateFlow("refresh", false)` and calls `viewModel.refresh()` when it becomes `true`, then resets it to `false`.

For language switch, which is two levels deep, the settings navigation helper pops to the index route directly (`popBackStack(CHAMPION_INDEX_ROUTE, inclusive = false)`) after setting the signal.

---

## Data flow

### Shared use case cache (critical)

`GetChampionDataUseCase` gains `@Singleton` scope so both `ChampionViewModel` and `SettingsViewModel` share the same instance. The existing `@VisibleForTesting setVersion()` is renamed to `reset()` (sets `cachedVersion = null`). All existing unit tests updated to call `reset()`.

### Refresh data flow
1. User confirms → `SettingsViewModel.refreshData()`:
   - `championRepository.clearLocalData()` — deletes all rows from `Champion` and `ChampionDetail` tables.
   - `appDataRepository.setLocalVersion("0")` — DataStore version reset.
   - `getChampionDataUseCase.reset()` — in-memory cache cleared.
2. Set `"refresh" = true` on index `SavedStateHandle` → `navController.popBackStack()`.
3. `ChampionIndexRoute` reads result → `viewModel.refresh()`.
4. Use case runs: local version `"0"` < remote → fetches fresh from network.

### Language switch flow
1. User selects language + confirms → `SettingsViewModel.selectLanguage(lang)`:
   - `appDataRepository.setLanguage(lang)`.
   - `championRepository.clearLocalData()`.
   - `appDataRepository.setLocalVersion("0")`.
   - `getChampionDataUseCase.reset()`.
2. Set `"refresh" = true` on index `SavedStateHandle` → pop to index.
3. Same refresh path as above; use case fetches champions in the new language.

### ChampionViewModel.refresh()
```kotlin
private val _refreshTrigger = MutableStateFlow(0L)

val champions = combine(_query.asStateFlow(), _refreshTrigger) { q, _ -> q }
    .debounce(300)
    .map { getChampionDataUseCase(it) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

fun refresh() { _refreshTrigger.value = System.currentTimeMillis() }
```

---

## New files

| File | Purpose |
|---|---|
| `ui/settings/SettingsViewModel.kt` | Loads languages from API; exposes `currentLanguage`, `languages: UiState<List<String>>`; `selectLanguage()`, `refreshData()` |
| `ui/settings/compose/SettingScreen.kt` | Replaces commented-out file; two-row settings screen |
| `ui/settings/compose/LanguageScreen.kt` | Language picker with loading/error states and confirm dialog |
| `ui/navigation/SettingsNavigation.kt` | Route constants + `NavGraphBuilder` extensions for both settings routes |

## Modified files

| File | Change |
|---|---|
| `GetChampionDataUseCase.kt` | Add `@Singleton`; rename `setVersion()` → `reset()` |
| `ChampionRepository` interface | Add `clearLocalData()` |
| `DefaultChampionRepository.kt` | Implement `clearLocalData()` |
| `ChampionDao.kt` | Add `deleteAllChampions()` + `deleteAllChampionDetails()` |
| `AppDataRepository` interface | Add `getSupportedLanguages(): List<String>` |
| `DefaultAppDataRepository.kt` | Implement via `api.getSupportLanguage()` |
| `ChampionViewModel.kt` | Add `_refreshTrigger` + `refresh()` |
| `ChampionIndexHeader.kt` | Uncomment gear `IconButton` |
| `ChampionNavHost.kt` | Wire settings navigation + SavedStateHandle result handling |
| `ChampionIndexNavigation.kt` | Pass `navController` into `ChampionIndexRoute` for result reading |
| `ChampionDetailViewModelTest.kt` | Update `setVersion()` → `reset()` call |
| `ChampionViewModelTest.kt` | Update `setVersion()` → `reset()` call |

---

## Out of scope

- Persisting the language preference across the language picker without confirming (selection is ephemeral until confirm).
- Version picker (the old commented-out version selection feature stays commented out).
- Error retry on the language list (error state shown, user can navigate back and re-enter).
