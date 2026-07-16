# Settings-as-a-Tab Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move Settings from a gear-icon-pushed screen into a third bottom-nav tab, remove the gear icon and version text from the Champions/Items headers, and surface both the app version and game version as rows inside Settings.

**Architecture:** `TOP_LEVEL_TABS` (already the single source of truth for the bottom nav bar) gains a third `NavTab` for Settings. A new shared helper computes tab slide-transition direction from each route's position in that list, replacing the old 2-tab hardcoded/partially-hardcoded transition logic. The gear icon and its `onSettingClick` plumbing are removed from the Champions/Items composables and nav wiring. Settings' `TopAppBar` becomes a back-arrow-less `LargeTopAppBar`, and `SettingsViewModel` gains a `gameVersion` StateFlow consumed by two new rows in the settings list.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Navigation-Compose, Hilt, JUnit4, MockK, Robolectric/Roborazzi.

## Global Constraints

- Package root: `com.zzy.champions`. `BuildConfig` (generated, `buildFeatures.buildConfig = true`) lives at `com.zzy.champions.BuildConfig`.
- All new/changed user-facing strings must be added to `app/src/main/res/values/strings.xml` **and** all 20 locale files under `app/src/main/res/values-*/strings.xml` (cs, de, el, es, fr, hu, in, it, ja, ko, pl, pt-rBR, ro, ru, th, tr, vi, zh-rCN, zh-rMY, zh-rTW), or `lint` fails the build with `MissingTranslation`.
- Verify with: `./gradlew lint testDebugUnitTest -P roborazzi.test.verify=true` — must end `BUILD SUCCESSFUL`.
- Regenerate a Roborazzi golden with: `./gradlew testDebugUnitTest --tests "<FQCN>" -P roborazzi.test.record=true`.
- `internal` declarations in `app/src/main/...` are visible from `app/src/test/...` in this project (confirmed via `categorizeItems` in `ItemViewModel.kt` being used directly from `ItemCategorizationTest.kt`) — no need to make test-only helpers `public`.
- Commit messages end with `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` (existing repo convention).
- Design reference: `docs/specs/2026-07-01-settings-tab-design.md`.

---

### Task 1: Register Settings as a third top-level tab

**Files:**
- Create: `app/src/main/res/drawable/ic_settings.xml`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/TopLevelDestinations.kt`
- Test: `app/src/test/java/com/zzy/champions/ui/navigation/TopLevelDestinationsTest.kt`

**Interfaces:**
- Consumes: `SETTINGS_ROUTE` (currently defined in `SettingsNavigation.kt` as `const val SETTINGS_ROUTE = "settings"` — unchanged), `R.string.settings` (existing string, already translated in all locales).
- Produces: `TOP_LEVEL_TABS` now has 3 entries, last one routed to `SETTINGS_ROUTE`. `TOP_LEVEL_ROUTES` (derived `Set<String>`) includes it too. Later tasks depend on this 3-entry list for transition direction math.

- [ ] **Step 1: Create the settings drawable**

Create `app/src/main/res/drawable/ic_settings.xml` (same 24dp single-path style as the existing `ic_champions.xml`/`ic_items.xml`, tinted at runtime by `NavigationBarItemDefaults`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M19.14,12.94c0.04,-0.3,0.06,-0.61,0.06,-0.94c0,-0.32,-0.02,-0.64,-0.07,-0.94l2.03,-1.58c0.18,-0.14,0.23,-0.41,0.12,-0.61l-1.92,-3.32c-0.12,-0.22,-0.37,-0.29,-0.59,-0.22l-2.39,0.96c-0.5,-0.38,-1.03,-0.7,-1.62,-0.94L14.4,2.81c-0.04,-0.24,-0.24,-0.41,-0.48,-0.41h-3.84c-0.24,0,-0.43,0.17,-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22,-0.08,-0.47,0,-0.59,0.22L2.74,8.87C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58c-0.18,0.14,-0.23,0.41,-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39,-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44,-0.17,0.47,-0.41l0.36,-2.54c0.59,-0.24,1.13,-0.56,1.62,-0.94l2.39,0.96c0.22,0.08,0.47,0,0.59,-0.22l1.92,-3.32c0.12,-0.22,0.07,-0.47,-0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0,-3.6,-1.62,-3.6,-3.6s1.62,-3.6,3.6,-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z"/>
</vector>
```

- [ ] **Step 2: Write the failing test**

Create `app/src/test/java/com/zzy/champions/ui/navigation/TopLevelDestinationsTest.kt`:

```kotlin
package com.zzy.champions.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelDestinationsTest {

    @Test
    fun topLevelTabs_includesSettingsAsThirdTab() {
        assertEquals(3, TOP_LEVEL_TABS.size)
        assertEquals(SETTINGS_ROUTE, TOP_LEVEL_TABS.last().route)
    }

    @Test
    fun topLevelTabs_settingsDoesNotRefreshOnDataChange() {
        val settingsTab = TOP_LEVEL_TABS.first { it.route == SETTINGS_ROUTE }
        assertFalse(settingsTab.refreshesOnDataChange)
    }

    @Test
    fun topLevelRoutes_includesSettingsRoute() {
        assertTrue(SETTINGS_ROUTE in TOP_LEVEL_ROUTES)
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.ui.navigation.TopLevelDestinationsTest"`
Expected: FAIL — `topLevelTabs_includesSettingsAsThirdTab` fails with `expected:<3> but was:<2>`.

- [ ] **Step 4: Add Settings to TOP_LEVEL_TABS**

In `app/src/main/java/com/zzy/champions/ui/navigation/TopLevelDestinations.kt`, change:

```kotlin
internal val TOP_LEVEL_TABS: List<NavTab> = listOf(
    NavTab(CHAMPION_INDEX_ROUTE, R.drawable.ic_champions, R.string.nav_champions, refreshesOnDataChange = true),
    NavTab(ITEMS_ROUTE, R.drawable.ic_items, R.string.nav_items, refreshesOnDataChange = true),
)
```

to:

```kotlin
internal val TOP_LEVEL_TABS: List<NavTab> = listOf(
    NavTab(CHAMPION_INDEX_ROUTE, R.drawable.ic_champions, R.string.nav_champions, refreshesOnDataChange = true),
    NavTab(ITEMS_ROUTE, R.drawable.ic_items, R.string.nav_items, refreshesOnDataChange = true),
    NavTab(SETTINGS_ROUTE, R.drawable.ic_settings, R.string.settings),
)
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.ui.navigation.TopLevelDestinationsTest"`
Expected: PASS (3 tests).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/drawable/ic_settings.xml app/src/main/java/com/zzy/champions/ui/navigation/TopLevelDestinations.kt app/src/test/java/com/zzy/champions/ui/navigation/TopLevelDestinationsTest.kt
git commit -m "feat: add Settings as a third top-level tab"
```

Note: at this point the bottom nav bar shows 3 icons and tapping Settings navigates there via `navigateSingleTopTo`, but Settings still has its old back-arrow app bar and 2-tab-only slide transitions (fixed in Task 3). The app still builds and all existing tests still pass.

---

### Task 2: Shared tab slide-direction helper

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/navigation/TabTransitions.kt`
- Test: `app/src/test/java/com/zzy/champions/ui/navigation/TabTransitionsTest.kt`

**Interfaces:**
- Consumes: `TOP_LEVEL_TABS` (from Task 1, now 3 entries), `CHAMPION_INDEX_ROUTE`, `ITEMS_ROUTE`, `SETTINGS_ROUTE`.
- Produces: `internal fun tabEnterDirection(ownRoute: String, initialRoute: String?): AnimatedContentTransitionScope.SlideDirection` and `internal fun tabExitDirection(ownRoute: String, targetRoute: String?): AnimatedContentTransitionScope.SlideDirection`, both in package `com.zzy.champions.ui.navigation`. Task 3 wires these into the nav graph's `enterTransition`/`exitTransition` blocks.

Both tabs on either side of a transition must agree on slide direction (verified against the existing pre-Task-1 behavior: Champions↔Items always slid as one continuous motion, never two screens moving independently). The shared rule: moving to a tab with a **higher** index in `TOP_LEVEL_TABS` slides `Start`; moving to a **lower** index slides `End`.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/zzy/champions/ui/navigation/TabTransitionsTest.kt`:

```kotlin
package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class TabTransitionsTest {

    @Test
    fun exitDirection_forwardToHigherIndexTab_isStart() {
        assertEquals(SlideDirection.Start, tabExitDirection(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.Start, tabExitDirection(ITEMS_ROUTE, SETTINGS_ROUTE))
        assertEquals(SlideDirection.Start, tabExitDirection(CHAMPION_INDEX_ROUTE, SETTINGS_ROUTE))
    }

    @Test
    fun exitDirection_backwardToLowerIndexTab_isEnd() {
        assertEquals(SlideDirection.End, tabExitDirection(ITEMS_ROUTE, CHAMPION_INDEX_ROUTE))
        assertEquals(SlideDirection.End, tabExitDirection(SETTINGS_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.End, tabExitDirection(SETTINGS_ROUTE, CHAMPION_INDEX_ROUTE))
    }

    @Test
    fun enterDirection_arrivingFromLowerIndexTab_isStart() {
        assertEquals(SlideDirection.Start, tabEnterDirection(ITEMS_ROUTE, CHAMPION_INDEX_ROUTE))
        assertEquals(SlideDirection.Start, tabEnterDirection(SETTINGS_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.Start, tabEnterDirection(SETTINGS_ROUTE, CHAMPION_INDEX_ROUTE))
    }

    @Test
    fun enterDirection_arrivingFromHigherIndexTab_isEnd() {
        assertEquals(SlideDirection.End, tabEnterDirection(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE))
        assertEquals(SlideDirection.End, tabEnterDirection(ITEMS_ROUTE, SETTINGS_ROUTE))
        assertEquals(SlideDirection.End, tabEnterDirection(CHAMPION_INDEX_ROUTE, SETTINGS_ROUTE))
    }

    @Test
    fun exitDirection_unknownTargetRoute_defaultsToEnd() {
        assertEquals(SlideDirection.End, tabExitDirection(CHAMPION_INDEX_ROUTE, "unknown"))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.ui.navigation.TabTransitionsTest"`
Expected: FAIL to compile — `tabExitDirection`/`tabEnterDirection` unresolved reference.

- [ ] **Step 3: Implement the helper**

Create `app/src/main/java/com/zzy/champions/ui/navigation/TabTransitions.kt`:

```kotlin
package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection

private fun tabIndex(route: String?): Int = TOP_LEVEL_TABS.indexOfFirst { it.route == route }

// Direction tabs slide when moving from `sourceRoute` to `destinationRoute`:
// Start when advancing to a tab further right in TOP_LEVEL_TABS, End when
// moving to one further left (or to an unrecognized route). Both the
// exiting tab and the entering tab use this same value for a given
// transition, which is what keeps the animation reading as one continuous
// slide instead of two screens moving independently.
private fun slideDirection(sourceRoute: String?, destinationRoute: String?): SlideDirection =
    if (tabIndex(destinationRoute) > tabIndex(sourceRoute)) SlideDirection.Start else SlideDirection.End

// For a tab's exitTransition: `ownRoute` is the tab being left, `targetRoute`
// is where the user is navigating to.
internal fun tabExitDirection(ownRoute: String, targetRoute: String?): SlideDirection =
    slideDirection(sourceRoute = ownRoute, destinationRoute = targetRoute)

// For a tab's enterTransition: `ownRoute` is the tab being entered,
// `initialRoute` is where the user navigated from.
internal fun tabEnterDirection(ownRoute: String, initialRoute: String?): SlideDirection =
    slideDirection(sourceRoute = initialRoute, destinationRoute = ownRoute)
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.ui.navigation.TabTransitionsTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/navigation/TabTransitions.kt app/src/test/java/com/zzy/champions/ui/navigation/TabTransitionsTest.kt
git commit -m "feat: add shared index-aware tab slide-direction helper"
```

---

### Task 3: Make Settings a real top-level tab in the nav graph

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/SettingsNavigation.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingContent.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt`

**Interfaces:**
- Consumes: `tabEnterDirection`/`tabExitDirection` (Task 2).
- Produces: `settingsScreen(onLanguageClick: () -> Unit, onRefreshDone: () -> Unit)` (drops `onBack`). `SettingsRoute(modifier, viewModel, onLanguageClick, onRefreshDone)` (drops `onBack`). `SettingAppbar(modifier: Modifier = Modifier)` (drops `onBack`). `onSettingClick` on `championIndexScreen`/`itemsScreen` is untouched here (removed in Task 5) — this task only changes transitions and the app bar/back-arrow.

This task has no new automated test (it's a navigation/visual restyle of existing, already-tested screens); verification is the full suite staying green plus a manual transition sanity check.

- [ ] **Step 1: Update SettingsNavigation.kt — drop onBack, use shared transitions**

Replace the whole file `app/src/main/java/com/zzy/champions/ui/navigation/SettingsNavigation.kt`:

```kotlin
package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.settings.compose.LanguageRoute
import com.zzy.champions.ui.settings.compose.SettingsRoute

const val SETTINGS_ROUTE = "settings"
const val LANGUAGE_ROUTE = "settings/language"

fun NavGraphBuilder.settingsScreen(
    onLanguageClick: () -> Unit,
    onRefreshDone: () -> Unit,
) {
    composable(
        route = SETTINGS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = tabEnterDirection(SETTINGS_ROUTE, initialState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = tabExitDirection(SETTINGS_ROUTE, targetState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
            )
        },
    ) {
        SettingsRoute(
            onLanguageClick = onLanguageClick,
            onRefreshDone = onRefreshDone,
        )
    }
}

fun NavGraphBuilder.languageScreen(
    onBack: () -> Unit,
    onLanguageSelected: () -> Unit,
) {
    composable(
        route = LANGUAGE_ROUTE,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION, easing = EaseIn))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION, easing = EaseOut))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION, easing = EaseIn))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION, easing = EaseOut))
        },
    ) {
        LanguageRoute(
            onBack = onBack,
            onLanguageSelected = onLanguageSelected,
        )
    }
}
```

(`languageScreen` is unchanged — Language is still pushed from within Settings with its own back arrow.)

- [ ] **Step 2: Update ChampionIndexNavigation.kt to use the shared transitions**

In `app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt`, replace the imports and `composable(...)` block:

```kotlin
package com.zzy.champions.ui.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.data.model.Champion
import com.zzy.champions.ui.index.compose.ChampionIndexRoute

const val CHAMPION_INDEX_ROUTE = "index"

fun NavGraphBuilder.championIndexScreen(
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit,
    onSplashFinished: () -> Unit = {},
) {
    composable(
        route = CHAMPION_INDEX_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = tabEnterDirection(CHAMPION_INDEX_ROUTE, initialState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = tabExitDirection(CHAMPION_INDEX_ROUTE, targetState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
            )
        }
    ) { backStackEntry ->
        val refreshStamp by backStackEntry.savedStateHandle
            .getStateFlow(KEY_REFRESH, 0)
            .collectAsStateWithLifecycle()

        ChampionIndexRoute(
            onItemClick = onItemClick,
            onSettingClick = onSettingClick,
            refreshStamp = refreshStamp,
            onStampConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = 0 },
            onSplashFinished = onSplashFinished,
        )
    }
}
```

(`onSettingClick` is intentionally kept for now — it's removed in Task 5 along with the gear icon it points at.)

- [ ] **Step 3: Update ItemsNavigation.kt to use the shared transitions**

Replace the whole file `app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt`:

```kotlin
package com.zzy.champions.ui.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.items.compose.ItemRoute

const val ITEMS_ROUTE = "items"

fun NavGraphBuilder.itemsScreen(onSettingClick: () -> Unit = {}) {
    composable(
        route = ITEMS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = tabEnterDirection(ITEMS_ROUTE, initialState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = tabExitDirection(ITEMS_ROUTE, targetState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
            )
        }
    ) { backStackEntry ->
        val refreshStamp by backStackEntry.savedStateHandle
            .getStateFlow(KEY_REFRESH, 0)
            .collectAsStateWithLifecycle()

        ItemRoute(
            onSettingClick = onSettingClick,
            refreshStamp = refreshStamp,
            onStampConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = 0 },
        )
    }
}
```

- [ ] **Step 4: Update SettingContent.kt — LargeTopAppBar, no back arrow**

Replace the whole file `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingContent.kt`:

```kotlin
package com.zzy.champions.ui.settings.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.data.model.SettingSelectable
import com.zzy.champions.ui.compose.ErrorBar
import com.zzy.champions.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingAppbar(modifier: Modifier = Modifier) {
    LargeTopAppBar(
        modifier = modifier,
        title = {
            Text(text = stringResource(id = R.string.settings))
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}
@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    itemName: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    role: Role = Role.Button,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .then(if (onClick != null) Modifier.minimumInteractiveComponentSize().clickable(role = role, onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemName,
                    color = MaterialTheme.colorScheme.onBackground
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            content()
        }
        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    gameVersion: String,
    dataVersions: List<SettingSelectable>,
    onAppVersionSelected: (String) -> Unit,
    languages: List<SettingSelectable>,
    onLanguageSelected: (String) -> Unit,
    showError: Boolean = false
) {
    Column(modifier) {
        if (showError) ErrorBar()
        SettingItem(
            itemName = stringResource(id = R.string.latest_game_version)
        ) {
            Text(
                text = gameVersion,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
        SettingItem(
            itemName = stringResource(id = R.string.app_data_version)
        ) {
            SelectableBottomMenu(
                list = dataVersions, onItemSelected = onAppVersionSelected
            )
        }
        SettingItem(
            itemName = stringResource(id = R.string.language)
        ) {
            SelectableBottomMenu(
                list = languages, onItemSelected = onLanguageSelected
            )
        }
    }
}

@Composable
@Preview
fun PreviewItem() {
    MyApplicationTheme {
        Scaffold(topBar = {
            SettingAppbar()
        }) { padding ->
            Settings(
                modifier = Modifier.padding(padding),
                gameVersion = "1.13.1",
                dataVersions = listOf(
                    SettingSelectable("1.13.1", false),
                    SettingSelectable("1.13.2", false),
                    SettingSelectable("1.13.3", true),
                    SettingSelectable("1.13.4", false),
                    SettingSelectable("1.13.5", false),
                    SettingSelectable("1.13.6", false),
                ),
                onAppVersionSelected = {

                },
                languages = emptyList(),
                onLanguageSelected = {

                })
        }
    }
}
```

(`Settings`/`SelectableBottomMenu` below `SettingItem` are pre-existing dead code, out of scope for this change — left untouched except for the `SettingAppbar()` call in the preview, which must drop the removed `onBack` argument to keep compiling.)

- [ ] **Step 5: Update SettingScreen.kt — drop onBack**

In `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt`, change the function signature and the `topBar`:

```kotlin
@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onLanguageClick: () -> Unit,
    onRefreshDone: () -> Unit,
) {
```

(drop the `onBack: () -> Unit,` parameter line), and:

```kotlin
    Scaffold(
        modifier = modifier,
        topBar = { SettingAppbar() },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
```

(was `topBar = { SettingAppbar(onBack = onBack) }`). Everything else in the file (the two `SettingItem` rows, the refresh dialog) is unchanged for this step.

- [ ] **Step 6: Update ChampionNavHost.kt — wire settingsScreen without onBack, stop popping on refresh**

In `app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt`, change the `settingsScreen(...)` call:

```kotlin
        settingsScreen(
            onLanguageClick = { navController.navigate(LANGUAGE_ROUTE) { launchSingleTop = true } },
            onRefreshDone = {
                navController.signalRefresh()
            }
        )
```

(was `onBack = { navController.popBackStack() }, onLanguageClick = ..., onRefreshDone = { navController.signalRefresh(); navController.popBackStack() }`). Settings is now a peer tab, not a pushed screen, so there's nothing to pop back to after a refresh — it just marks the other tabs' data stale via `signalRefresh()` and the user stays on Settings. `championIndexScreen(...)` and `itemsScreen(...)` calls are unchanged in this step (still pass `onSettingClick`).

- [ ] **Step 7: Run full verification**

Run: `./gradlew lint testDebugUnitTest -P roborazzi.test.verify=true`
Expected: `BUILD SUCCESSFUL`. (No test yet covers the app bar/back-arrow removal directly, but nothing should regress — `SettingsViewModelTest` doesn't touch navigation, and `ChampionIndexScreenshotTest`/`ItemScreen` previews still compile since `onSettingClick` is untouched in this task.)

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/navigation/SettingsNavigation.kt app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt app/src/main/java/com/zzy/champions/ui/settings/compose/SettingContent.kt app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt
git commit -m "feat: make Settings a real top-level tab (transitions, app bar)"
```

---

### Task 4: App Version and Game Version rows in Settings

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: all 20 files matching `app/src/main/res/values-*/strings.xml`
- Test: `app/src/test/java/com/zzy/champions/settings/SettingsViewModelTest.kt`

**Interfaces:**
- Consumes: `AppDataRepository.getLocalVersion(): Flow<String>` (existing), `BuildConfig.VERSION_NAME` (generated), `R.string.app_version` (new), `R.string.latest_game_version` (existing), `R.string.v_` (existing, `"v%s"` format).
- Produces: `SettingsViewModel.gameVersion: StateFlow<String>`.

- [ ] **Step 1: Write the failing test**

In `app/src/test/java/com/zzy/champions/settings/SettingsViewModelTest.kt`, add the `assertEquals` import and a stub for `getLocalVersion()` in `setup()`, plus a new test. Change the import block:

```kotlin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
```

(was `import org.junit.Assert.assertNull` / `import org.junit.Assert.assertTrue` only — add the `assertEquals` line).

In `setup()`, add one line after the existing `coEvery` stubs:

```kotlin
        coEvery { appDataRepository.getLanguage() } returns flowOf("en_US")
        coEvery { appDataRepository.getSupportedLanguages() } returns listOf("en_US", "zh_CN", "ko_KR")
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)

        viewModel = SettingsViewModel(
            appDataRepository, championRepository, getChampionDataUseCase, getItemDataUseCase, Dispatchers.Main
        )
    }
```

Then add a new test method (anywhere after `setup()`, e.g. right after `languagesLoadedOnInit`):

```kotlin
    @Test
    fun gameVersionExposesLocalVersion() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.gameVersion.collect() }
        advanceUntilIdle()
        assertEquals(VERSION_14_0, viewModel.gameVersion.value)
        job.cancel()
    }
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.settings.SettingsViewModelTest"`
Expected: FAIL — `gameVersionExposesLocalVersion` fails (either `Unresolved reference: gameVersion` at compile time, or once compiling, an `io.mockk.MockKException` from the unstubbed `getLocalVersion()` call if the stub line was skipped — either way, red before Step 3).

- [ ] **Step 3: Add gameVersion to SettingsViewModel**

In `app/src/main/java/com/zzy/champions/ui/settings/SettingsViewModel.kt`, add right after the `currentLanguage` property:

```kotlin
    val currentLanguage: StateFlow<String> = appDataRepository.getLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val gameVersion: StateFlow<String> = appDataRepository.getLocalVersion()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.settings.SettingsViewModelTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Add the app_version string to the default and all 20 locale files**

In `app/src/main/res/values/strings.xml`, change:

```xml
    <string name="settings">Settings</string>
    <string name="latest_game_version">Latest game version: </string>
    <string name="app_data_version">App data version: </string>
    <string name="language">Language</string>
```

to:

```xml
    <string name="settings">Settings</string>
    <string name="latest_game_version">Latest game version: </string>
    <string name="app_data_version">App data version: </string>
    <string name="app_version">App version: </string>
    <string name="language">Language</string>
```

Apply the equivalent one-line insertion (immediately after each file's `app_data_version` string) to all 20 locale files, using these translations:

| Locale | Insert after `app_data_version` |
|---|---|
| `values-cs` | `<string name="app_version">Verze aplikace: </string>` |
| `values-de` | `<string name="app_version">App-Version: </string>` |
| `values-el` | `<string name="app_version">Έκδοση εφαρμογής: </string>` |
| `values-es` | `<string name="app_version">Versión de la app: </string>` |
| `values-fr` | `<string name="app_version">Version de l\'app : </string>` |
| `values-hu` | `<string name="app_version">Alkalmazás verziója: </string>` |
| `values-in` | `<string name="app_version">Versi aplikasi: </string>` |
| `values-it` | `<string name="app_version">Versione app: </string>` |
| `values-ja` | `<string name="app_version">アプリバージョン：</string>` |
| `values-ko` | `<string name="app_version">앱 버전: </string>` |
| `values-pl` | `<string name="app_version">Wersja aplikacji: </string>` |
| `values-pt-rBR` | `<string name="app_version">Versão do app: </string>` |
| `values-ro` | `<string name="app_version">Versiunea aplicației: </string>` |
| `values-ru` | `<string name="app_version">Версия приложения: </string>` |
| `values-th` | `<string name="app_version">เวอร์ชั่นแอป: </string>` |
| `values-tr` | `<string name="app_version">Uygulama sürümü: </string>` |
| `values-vi` | `<string name="app_version">Phiên bản ứng dụng: </string>` |
| `values-zh-rCN` | `<string name="app_version">应用版本：</string>` |
| `values-zh-rMY` | `<string name="app_version">应用版本：</string>` |
| `values-zh-rTW` | `<string name="app_version">應用程式版本：</string>` |

Each locale's `app_data_version` line (from `grep -H "app_data_version" app/src/main/res/values-*/strings.xml`) is unique within its file, so `old_string`/`new_string` anchored on that single line is safe for a targeted edit in each file.

- [ ] **Step 6: Add the two version rows to SettingsRoute**

In `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt`, add two new import lines. Change:

```kotlin
import androidx.hilt.navigation.compose.hiltViewModel
import com.zzy.champions.R
```

to:

```kotlin
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.BuildConfig
import com.zzy.champions.R
```

Then, inside `SettingsRoute`, add the state read right after `var showRefreshDialog by remember { mutableStateOf(false) }`:

```kotlin
    var showRefreshDialog by remember { mutableStateOf(false) }
    val gameVersion by viewModel.gameVersion.collectAsStateWithLifecycle()
```

Finally, append two more `SettingItem` rows inside the `Column(Modifier.padding(padding))` block, after the existing "Refresh Data" `SettingItem`:

```kotlin
            SettingItem(
                itemName = stringResource(R.string.refresh_data),
                description = stringResource(R.string.refresh_data_desc),
                onClick = { showRefreshDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            }
            SettingItem(itemName = stringResource(R.string.app_version)) {
                Text(
                    text = BuildConfig.VERSION_NAME,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            SettingItem(itemName = stringResource(R.string.latest_game_version)) {
                Text(
                    text = stringResource(R.string.v_, gameVersion),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
```

- [ ] **Step 7: Run full verification**

Run: `./gradlew lint testDebugUnitTest -P roborazzi.test.verify=true`
Expected: `BUILD SUCCESSFUL`. If `lint` reports `MissingTranslation` for `app_version`, one of the 20 locale files in Step 5 was missed — check `grep -L "app_version" app/src/main/res/values-*/strings.xml` (should print nothing).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/settings/SettingsViewModel.kt app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt app/src/main/res/values/strings.xml app/src/main/res/values-*/strings.xml app/src/test/java/com/zzy/champions/settings/SettingsViewModelTest.kt
git commit -m "feat: show app version and game version in Settings"
```

---

### Task 5: Remove the gear icon and version text from Champions/Items

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexHeader.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexScreen.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/compose/UtilComponents.kt`
- Modify: `app/src/test/java/com/zzy/champions/index/ChampionIndexScreenshotTest.kt`
- Test image: `app/src/test/screenshots/ChampionIndexScreen_phone.png` (regenerated, not hand-edited)

**Interfaces:**
- Consumes: nothing new.
- Produces: `Header()` (no params), `ChampionIndexScreen(...)` and `ChampionIndexRoute(...)` drop `onSettingClick`, `ItemScreen(...)` and `ItemRoute(...)` drop `onSettingClick`, `championIndexScreen(...)` and `itemsScreen(...)` drop `onSettingClick`. `VersionText` is deleted (no remaining callers after this task).

- [ ] **Step 1: Simplify Header (champion index) — no gear icon, no version text**

Replace the whole file `app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexHeader.kt`:

```kotlin
package com.zzy.champions.ui.index.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R

@Composable
fun Header(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.choose_your).uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 10.sp
        )
        Text(
            text = stringResource(id = R.string.champion).uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 36.sp,
            fontWeight = FontWeight(900),
            fontStyle = FontStyle.Italic
        )
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.choose_your_champion_desc),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 10.sp,
            lineHeight = 10.sp
        )
    }
}
```

(The outer `Box` from the old version only existed to position the gear `IconButton` at `TopEnd`; with the icon gone, a plain `Column` replaces it.)

- [ ] **Step 2: Drop onSettingClick from ChampionIndexScreen.kt**

Replace the whole file `app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexScreen.kt`:

```kotlin
package com.zzy.champions.ui.index.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.local.ChampionDataPreviewParameterProvider
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.compose.LaunchScreen
import com.zzy.champions.ui.index.ChampionViewModel
import com.zzy.champions.ui.theme.MyApplicationTheme


@Composable
fun ChampionIndexRoute(
    modifier: Modifier = Modifier,
    viewModel: ChampionViewModel = hiltViewModel(),
    onItemClick: (Champion) -> Unit,
    refreshStamp: Int = 0,
    onStampConsumed: () -> Unit = {},
    onSplashFinished: () -> Unit = {},
) {
    LaunchedEffect(refreshStamp) {
        if (refreshStamp > 0) {
            viewModel.refresh()
            onStampConsumed()
        }
    }

    val champions by viewModel.champions.collectAsStateWithLifecycle()

    ChampionIndexScreen(
        modifier = modifier,
        championsState = champions,
        onUpdateSearchKeyword = viewModel::updateSearchKeyword,
        onInsertBuilds = {},
        onItemClick = onItemClick,
        onSplashFinished = onSplashFinished,
    )
}

@Composable
fun ChampionIndexScreen(
    modifier: Modifier = Modifier,
    onboardingShowLandingScreen: Boolean = true,
    championsState: UiState<ChampionData>,
    onUpdateSearchKeyword: (String) -> Unit,
    onInsertBuilds: () -> Unit,
    onItemClick: (Champion) -> Unit,
    onSplashFinished: () -> Unit = {},
) {
    var showLandingScreen by rememberSaveable { mutableStateOf(onboardingShowLandingScreen) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun clearSearchTextAndReloadAllChampions() {
        searchText = ""
        onUpdateSearchKeyword(searchText)
    }

    LaunchedEffect(Unit) {
        onInsertBuilds()
        onUpdateSearchKeyword(searchText)
    }

    BackHandler(enabled = searchText.isNotBlank()) {
        clearSearchTextAndReloadAllChampions()
    }

    if (!showLandingScreen && championsState is UiState.Success) {
        Column(modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Header()
            SearchTextField(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = searchText,
                onTextChanged = {
                    searchText = it
                    onUpdateSearchKeyword(searchText)
                },
                onClearText = {
                    clearSearchTextAndReloadAllChampions()
                },
                onDone = {
                    keyboardController?.hide()
                    onUpdateSearchKeyword(it)
                }
            )
            LazyVerticalGrid(
                modifier = Modifier.padding(top = 12.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(championsState.data.champions) {
                    ChampionCard(version = championsState.data.version, champion = it) {
                        onItemClick(it)
                    }
                }
            }
        }
    } else {
        LaunchScreen(modifier = Modifier, onTimeout = {
            showLandingScreen = false
            onSplashFinished()
        })
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewChampionIndexScreen(
    @PreviewParameter(ChampionDataPreviewParameterProvider::class)
    championData: ChampionData
) {
    MyApplicationTheme {
        Scaffold { padding ->
            ChampionIndexScreen(
                modifier = Modifier.padding(padding),
                onboardingShowLandingScreen = false,
                championsState = UiState.Success(championData),
                onUpdateSearchKeyword = {},
                onInsertBuilds = { },
                onItemClick = { }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLandingScreen(
    @PreviewParameter(ChampionDataPreviewParameterProvider::class)
    championData: ChampionData
) {
    MyApplicationTheme {
        Scaffold { padding ->
            ChampionIndexScreen(
                modifier = Modifier.padding(padding),
                onboardingShowLandingScreen = true,
                championsState = UiState.Success(championData),
                onUpdateSearchKeyword = {},
                onInsertBuilds = { },
                onItemClick = { }
            )
        }
    }
}
```

(`ChampionCard(version = championsState.data.version, ...)` is untouched — that `version` comes from `championsState`, not from the removed `Header`/`VersionText`.)

- [ ] **Step 3: Remove ItemsHeader entirely from ItemScreen.kt**

Replace the whole file `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`:

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.zzy.champions.ui.items.ItemViewModel
import com.zzy.champions.ui.theme.Golden

private const val GRID_COLUMNS = 5
internal val itemCutCornerShape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
private val categoryHeaderBrush = Brush.horizontalGradient(listOf(Golden.copy(alpha = 0.25f), Color.Transparent))

private val categoryNameResIds = mapOf(
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

    val categorizedState by viewModel.categorizedItems.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val version by viewModel.version.collectAsStateWithLifecycle()
    val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler(enabled = searchText.isNotBlank()) {
        viewModel.updateSearchQuery("")
    }

    ItemScreen(
        modifier = modifier,
        categorizedState = categorizedState,
        version = version,
        searchText = searchText,
        onSearchTextChange = { viewModel.updateSearchQuery(it) },
        onSearchDone = { keyboardController?.hide() },
        onClearSearch = { viewModel.updateSearchQuery("") },
        onItemClick = viewModel::selectItem,
        onReloadClick = viewModel::retry,
    )

    val resolveItem = remember(viewModel) { viewModel::getItemById }
    val onComponentClick = remember(viewModel) { { componentId: String ->
        val resolved = resolveItem(componentId)
        if (resolved != null) viewModel.selectItem(resolved)
    } }
    // Don't overlay the error screen: dismiss the sheet when the load fails so
    // the user can reach the reload button.
    selectedItem?.takeIf { categorizedState !is UiState.Error }?.let { item ->
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
    categorizedState: UiState<List<Pair<String, List<Item>>>>,
    version: String,
    searchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    onSearchDone: () -> Unit = {},
    onClearSearch: (() -> Unit)? = null,
    onItemClick: (Item) -> Unit,
    onReloadClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        SearchTextField(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = searchText,
            onTextChanged = onSearchTextChange,
            onClearText = onClearSearch,
            onDone = { onSearchDone() },
        )
        when (categorizedState) {
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
                    categorizedState.data.forEach { (categoryName, categoryItems) ->
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

(`ItemsHeader` only ever rendered the gear icon and `VersionText` — with both gone it had nothing left to render, so the whole composable and its call site are removed. `SearchTextField`'s vertical padding bumps from `4.dp` to `8.dp` to keep roughly the same top spacing the removed header used to provide.)

- [ ] **Step 4: Drop onSettingClick from ChampionIndexNavigation.kt**

In `app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt`, remove the `onSettingClick: () -> Unit,` parameter from `championIndexScreen(...)` and the `onSettingClick = onSettingClick,` line from the `ChampionIndexRoute(...)` call:

```kotlin
fun NavGraphBuilder.championIndexScreen(
    onItemClick: (Champion) -> Unit,
    onSplashFinished: () -> Unit = {},
) {
```

```kotlin
        ChampionIndexRoute(
            onItemClick = onItemClick,
            refreshStamp = refreshStamp,
            onStampConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = 0 },
            onSplashFinished = onSplashFinished,
        )
```

- [ ] **Step 5: Drop onSettingClick from ItemsNavigation.kt**

In `app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt`, change the function signature and body:

```kotlin
fun NavGraphBuilder.itemsScreen() {
```

```kotlin
        ItemRoute(
            refreshStamp = refreshStamp,
            onStampConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = 0 },
        )
```

- [ ] **Step 6: Drop onSettingClick wiring from ChampionNavHost.kt**

In `app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt`, change:

```kotlin
        championIndexScreen(
            onItemClick = { navController.navigateToChampionDetail(it.id) },
            onSettingClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } },
            onSplashFinished = onSplashFinished,
        )
```

to:

```kotlin
        championIndexScreen(
            onItemClick = { navController.navigateToChampionDetail(it.id) },
            onSplashFinished = onSplashFinished,
        )
```

and:

```kotlin
        itemsScreen(
            onSettingClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } },
        )
```

to:

```kotlin
        itemsScreen()
```

- [ ] **Step 7: Delete the now-unused VersionText from UtilComponents.kt**

Replace the whole file `app/src/main/java/com/zzy/champions/ui/compose/UtilComponents.kt`:

```kotlin
package com.zzy.champions.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zzy.champions.R

@Composable
fun ErrorBar(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.fillMaxWidth().background(Color.Red.copy(alpha = 0.5f)).padding(8.dp),
        text = stringResource(id = R.string.internet_connection_error),
        color = Color.White
    )
}
```

- [ ] **Step 8: Update ChampionIndexScreenshotTest.kt to drop onSettingClick**

In `app/src/test/java/com/zzy/champions/index/ChampionIndexScreenshotTest.kt`, remove both `onSettingClick = { },` lines (one per test method) and the dead commented-out `captureRoboImage` block at the end of `championIndexScreen()`:

```kotlin
    @Test
    fun championIndexOnboardingLandingScreen() {
        composeTestRule.captureForPhone("ChampionIndexOnboardingLandingScreen") {
            MyApplicationTheme {
                Scaffold { padding ->
                    ChampionIndexScreen(
                        modifier = Modifier.padding(padding),
                        championsState = UiState.Loading,
                        onboardingShowLandingScreen = true,
                        onUpdateSearchKeyword = {

                        },
                        onInsertBuilds = { },
                        onItemClick = {

                        })
                }
            }
        }
    }

    @Test
    fun championIndexScreen() {
        composeTestRule.captureForPhone("ChampionIndexScreen") {
            MyApplicationTheme {
                Scaffold { padding ->
                    ChampionIndexScreen(
                        modifier = Modifier.padding(padding),
                        championsState = UiState.Success(championData),
                        onboardingShowLandingScreen = false,
                        onUpdateSearchKeyword = {

                        },
                        onInsertBuilds = {  },
                        onItemClick = {
                        })
                }
            }
        }
    }
}
```

- [ ] **Step 9: Run the screenshot test to see it fail against the stale golden**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.index.ChampionIndexScreenshotTest" -P roborazzi.test.verify=true`
Expected: FAIL — `championIndexScreen` fails Roborazzi comparison (header is now shorter: no gear icon, no version line).

- [ ] **Step 10: Regenerate the golden and inspect the diff**

Run: `./gradlew testDebugUnitTest --tests "com.zzy.champions.index.ChampionIndexScreenshotTest" -P roborazzi.test.record=true`

Then open `app/build/outputs/roborazzi/ChampionIndexScreen_phone_compare.png` and confirm the only differences are: no gear icon in the top-right, and no small version text under the description — nothing else shifted unexpectedly.

- [ ] **Step 11: Run full verification**

Run: `./gradlew lint testDebugUnitTest -P roborazzi.test.verify=true`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 12: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexHeader.kt app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexScreen.kt app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt app/src/main/java/com/zzy/champions/ui/compose/UtilComponents.kt app/src/test/java/com/zzy/champions/index/ChampionIndexScreenshotTest.kt app/src/test/screenshots/ChampionIndexScreen_phone.png
git commit -m "feat: remove settings gear icon and version text from Champions/Items headers"
```

---

### Task 6: Final verification

**Files:** none (verification only).

- [ ] **Step 1: Full lint + unit test run**

Run: `./gradlew lint testDebugUnitTest -P roborazzi.test.verify=true`
Expected: `BUILD SUCCESSFUL`, 0 lint errors.

- [ ] **Step 2: Confirm no leftover references**

Run: `grep -rn "onSettingClick\|VersionText" app/src/main/java app/src/test`
Expected: no output (both fully removed).

- [ ] **Step 3: Manual sanity check (if a device/emulator is available)**

Launch the app and confirm: bottom nav shows Champions / Items / Settings; Champions and Items headers no longer show a gear icon or version text; tapping through all three tabs in both directions slides content the same visual way it did for Champions↔Items before this change; Settings shows a large left-aligned "Settings" title with no back arrow; the settings list ends with App Version and Game Version rows showing real values.

- [ ] **Step 4: Push the branch**

```bash
git push -u origin feature/settings-tab
```
