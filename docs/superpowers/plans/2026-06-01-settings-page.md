# Settings Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a settings screen (gear icon entry point, switch language, refresh data with confirmation) to the champion index app.

**Architecture:** New `SettingScreen` and `LanguageScreen` Compose destinations added via `SettingsNavigation.kt`. A new `SettingsViewModel` coordinates data clearing and language switching. `GetChampionDataUseCase` becomes `@Singleton` so its in-memory cache can be reset from either ViewModel. `ChampionViewModel` gains a `refresh()` trigger; the index screen reads a `"refresh"` signal from the navigation `SavedStateHandle` on return from settings and calls `refresh()`.

**Tech Stack:** Jetpack Compose, Hilt, Room, Compose Navigation, Kotlin Coroutines/Flow, Material3 AlertDialog

**Spec:** `docs/superpowers/specs/2026-06-01-settings-page-design.md`

**Commands:** Use `./gradlew` (Git Bash) or `gradlew.bat` (cmd/PowerShell) — they are equivalent.

---

## File Map

### New
- `app/src/main/java/com/zzy/champions/ui/settings/SettingsViewModel.kt`
- `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt` _(replaces commented-out stub)_
- `app/src/main/java/com/zzy/champions/ui/settings/compose/LanguageScreen.kt`
- `app/src/main/java/com/zzy/champions/ui/navigation/SettingsNavigation.kt`
- `app/src/test/java/com/zzy/champions/settings/SettingsViewModelTest.kt`

### Modified
| File | Change |
|---|---|
| `data/local/db/ChampionDao.kt` | Uncomment `clearChampions()`; add `clearAll()` transaction |
| `data/repository/ChampionRepository.kt` | Add `clearLocalData()` |
| `data/repository/DefaultChampionRepository.kt` | Implement `clearLocalData()` |
| `data/repository/AppDataRepository.kt` | Add `getSupportedLanguages()` |
| `data/repository/DefaultAppDataRepository.kt` | Implement `getSupportedLanguages()` |
| `domain/GetChampionDataUseCase.kt` | Add `@Singleton`; add `reset()` |
| `ui/index/ChampionViewModel.kt` | Add `_refreshTrigger` + `refresh()` |
| `ui/index/compose/ChampionIndexScreen.kt` | `ChampionIndexRoute` reads SavedStateHandle result |
| `ui/index/compose/ChampionIndexHeader.kt` | Uncomment gear `IconButton` |
| `ui/navigation/ChampionIndexNavigation.kt` | Pass `shouldRefresh` + `onRefreshConsumed` into `ChampionIndexRoute` |
| `ui/navigation/ChampionNavHost.kt` | Wire settings routes + gear icon callback |
| `res/values/strings.xml` | Add 9 strings |
| `test/.../TestChampionRepository.kt` | Implement `clearLocalData()` |
| `test/.../index/ChampionViewModelTest.kt` | Add `refresh_causesReload` test |

---

### Task 1: ChampionDao — expose clear queries

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/data/local/db/ChampionDao.kt`

- [ ] **Step 1: Uncomment the existing delete query and add `clearAll()`**

Replace the commented block and the class body so it reads:

```kotlin
//    @Query("DELETE FROM champion")         // ← remove this comment block
//    abstract suspend fun clearChampions()

@Query("DELETE FROM champion")
abstract suspend fun clearChampions()

@Transaction
open suspend fun clearAll() {
    clearChampions()
    clearDetailData()
}
```

The full modified `ChampionDao` (show only new/changed lines; everything else stays):

```kotlin
@Dao
abstract class ChampionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertChampions(champions: List<Champion>)

    @Query("SELECT * FROM champion WHERE id LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    abstract suspend fun queryChampionsById(query: String): List<Champion>

    @Upsert(entity = ChampionDetail::class)
    abstract suspend fun insertChampionDetail(detail: ChampionDetail)

    @Query("SELECT * FROM ChampionDetail WHERE `championId` = :id  LIMIT 1")
    abstract suspend fun getChampionDetail(id: String): ChampionDetail?

    @Transaction
    @Query("SELECT * FROM champion WHERE `id` = :id  LIMIT 1")
    abstract suspend fun getChampionAndDetail(id: String): ChampionAndDetail

    @Query("DELETE FROM championdetail")
    abstract suspend fun clearDetailData()

    @Query("DELETE FROM champion")
    abstract suspend fun clearChampions()

    @Transaction
    open suspend fun clearAll() {
        clearChampions()
        clearDetailData()
    }

    @Transaction
    open suspend fun updateLocalChampionData(champions: List<Champion>) {
        insertChampions(champions)
        clearDetailData()
    }
}
```

- [ ] **Step 2: Build to confirm codegen succeeds**

```bash
./gradlew :app:kspDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/zzy/champions/data/local/db/ChampionDao.kt
git commit -m "feat: add clearAll query to ChampionDao"
```

---

### Task 2: ChampionRepository — clearLocalData()

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/data/repository/ChampionRepository.kt`
- Modify: `app/src/main/java/com/zzy/champions/data/repository/DefaultChampionRepository.kt`
- Modify: `app/src/test/java/com/zzy/champions/TestChampionRepository.kt`

- [ ] **Step 1: Add `clearLocalData()` to the `ChampionRepository` interface**

```kotlin
interface ChampionRepository {
    suspend fun getRemoteChampions(version: String, language: String): ChampionResponse
    suspend fun saveLocalChampions(champions: List<Champion>)
    suspend fun searchChampionsBy(id: String): List<Champion>
    suspend fun getRemoteChampionDetail(version: String, language: String, id: String): ChampionDetail?
    suspend fun getLocalChampionDetail(championId: String): ChampionDetail?
    suspend fun saveChampionDetail(championDetail: ChampionDetail)
    suspend fun getChampionAndDetail(id: String): ChampionAndDetail
    suspend fun clearLocalData()   // ← add this
}
```

- [ ] **Step 2: Implement in `DefaultChampionRepository`**

Add after `getChampionAndDetail`:

```kotlin
override suspend fun clearLocalData() = withContext(dispatcher) {
    dao.clearAll()
}
```

- [ ] **Step 3: Implement in `TestChampionRepository`**

Add after `getChampionAndDetail`:

```kotlin
override suspend fun clearLocalData() {
    localChampions = emptyList()
    localChampionDetails.clear()
}
```

- [ ] **Step 4: Build to confirm no compile errors**

```bash
./gradlew :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/zzy/champions/data/repository/ChampionRepository.kt \
        app/src/main/java/com/zzy/champions/data/repository/DefaultChampionRepository.kt \
        app/src/test/java/com/zzy/champions/TestChampionRepository.kt
git commit -m "feat: add clearLocalData to ChampionRepository"
```

---

### Task 3: AppDataRepository — getSupportedLanguages()

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/data/repository/AppDataRepository.kt`
- Modify: `app/src/main/java/com/zzy/champions/data/repository/DefaultAppDataRepository.kt`

- [ ] **Step 1: Add to the interface**

```kotlin
interface AppDataRepository {
    suspend fun getRemoteVersion(): List<String>
    fun getLocalVersion(): Flow<String>
    suspend fun setLocalVersion(v: String)
    fun getLanguage(): Flow<String>
    suspend fun setLanguage(l: String)
    suspend fun getSupportedLanguages(): List<String>   // ← add this
}
```

- [ ] **Step 2: Implement in `DefaultAppDataRepository`**

```kotlin
override suspend fun getSupportedLanguages(): List<String> = api.getSupportLanguage()
```

- [ ] **Step 3: Build**

```bash
./gradlew :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/zzy/champions/data/repository/AppDataRepository.kt \
        app/src/main/java/com/zzy/champions/data/repository/DefaultAppDataRepository.kt
git commit -m "feat: add getSupportedLanguages to AppDataRepository"
```

---

### Task 4: GetChampionDataUseCase — @Singleton + reset()

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/domain/GetChampionDataUseCase.kt`

The `@Singleton` annotation means Hilt creates one shared instance for the whole app lifetime. Both `ChampionViewModel` and `SettingsViewModel` will receive this same instance. The `reset()` method nulls the in-memory `cachedVersion` so the next `invoke()` call re-checks versions and re-fetches.

- [ ] **Step 1: Add `@Singleton` and `reset()` to `GetChampionDataUseCase`**

```kotlin
import jakarta.inject.Singleton

@Singleton  // ← add above the class
class GetChampionDataUseCase @Inject constructor(
    private val championRepository: ChampionRepository,
    private val appDataRepository: AppDataRepository,
    private val dispatcher: CoroutineDispatcher,
) {
    private var cachedVersion: String? = null

    suspend operator fun invoke(query: String): UiState<ChampionData> = withContext(dispatcher) {
        // ... unchanged ...
    }

    fun reset() { cachedVersion = null }   // ← add this (no @VisibleForTesting — called by SettingsViewModel)

    @VisibleForTesting
    fun getVersion() = cachedVersion

    @VisibleForTesting
    fun setVersion(version: String) { this.cachedVersion = version }  // keep for tests
}
```

Note: use `import jakarta.inject.Singleton` (not `javax.inject.Singleton`) since the project uses Hilt with Jakarta annotations.

- [ ] **Step 2: Build**

```bash
./gradlew :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run existing use-case tests to confirm nothing broke**

```bash
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.domain.VersionInfoTest" \
    --tests "com.zzy.champions.index.ChampionViewModelTest" --console=plain
```

Expected: `BUILD SUCCESSFUL`, all tests pass.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/zzy/champions/domain/GetChampionDataUseCase.kt
git commit -m "feat: make GetChampionDataUseCase singleton and add reset()"
```

---

### Task 5: ChampionViewModel — refresh()

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/index/ChampionViewModel.kt`
- Modify: `app/src/test/java/com/zzy/champions/index/ChampionViewModelTest.kt`

`refresh()` updates `_refreshTrigger` which is combined with `_query`. The `combine` re-emits whenever either source changes, forcing the use case to run again even if the query text is unchanged.

- [ ] **Step 1: Write the failing test first**

Add to `ChampionViewModelTest.kt`:

```kotlin
@Test
fun refresh_causesReload() = runTest {
    // Arrange: local = remote = 14.0 → no update, serve local (empty)
    coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
    coEvery { appDataRepository.getRemoteVersion() } returns listOf(VERSION_14_0)
    coJustRun { appDataRepository.setLocalVersion(any()) }

    val collectJob = launch { viewModel.champions.collect() }
    advanceUntilIdle()

    // Use case cached 14.0; now settings resets it and remote is newer
    getChampionDataUseCase.reset()
    coEvery { appDataRepository.getRemoteVersion() } returns listOf(VERSION_14_1)

    // Act
    viewModel.refresh()
    advanceUntilIdle()

    // Assert: version updated to 14.1
    assertEquals(VERSION_14_1, getChampionDataUseCase.getVersion())

    collectJob.cancel()
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
./gradlew :app:testDebugUnitTest \
    --tests "com.zzy.champions.index.ChampionViewModelTest.refresh_causesReload" --console=plain
```

Expected: FAIL — `ChampionViewModel` has no `refresh()` method yet.

- [ ] **Step 3: Update `ChampionViewModel`**

Replace the entire file:

```kotlin
package com.zzy.champions.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.domain.GetChampionDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChampionViewModel @Inject constructor(
    private val getChampionDataUseCase: GetChampionDataUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _refreshTrigger = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val champions: StateFlow<UiState<ChampionData>> =
        combine(_query, _refreshTrigger) { q, _ -> q }
            .debounce(300)
            .map { getChampionDataUseCase(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading
            )

    fun updateSearchKeyword(query: String) { _query.value = query }

    fun refresh() { _refreshTrigger.value = System.currentTimeMillis() }
}
```

- [ ] **Step 4: Run all ChampionViewModel tests**

```bash
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.index.ChampionViewModelTest" --console=plain
```

Expected: `BUILD SUCCESSFUL`, all tests pass including `refresh_causesReload`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/index/ChampionViewModel.kt \
        app/src/test/java/com/zzy/champions/index/ChampionViewModelTest.kt
git commit -m "feat: add refresh() to ChampionViewModel"
```

---

### Task 6: SettingsViewModel

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/settings/SettingsViewModel.kt`
- Create: `app/src/test/java/com/zzy/champions/settings/SettingsViewModelTest.kt`

- [ ] **Step 1: Write the failing tests**

Create `app/src/test/java/com/zzy/champions/settings/SettingsViewModelTest.kt`:

```kotlin
package com.zzy.champions.settings

import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestChampionRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.domain.GetChampionDataUseCase
import com.zzy.champions.ui.settings.SettingsViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var appDataRepository: AppDataRepository

    private val championRepository = TestChampionRepository()
    private lateinit var getChampionDataUseCase: GetChampionDataUseCase
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getChampionDataUseCase = GetChampionDataUseCase(
            championRepository, appDataRepository, Dispatchers.Main
        )
        coEvery { appDataRepository.getLanguage() } returns flowOf("en_US")
        coEvery { appDataRepository.getSupportedLanguages() } returns listOf("en_US", "zh_CN", "ko_KR")

        viewModel = SettingsViewModel(
            appDataRepository, championRepository, getChampionDataUseCase, Dispatchers.Main
        )
    }

    @Test
    fun languagesLoadedOnInit() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.languages.collect() }
        assertTrue(viewModel.languages.value is UiState.Success)
        val langs = (viewModel.languages.value as UiState.Success).data
        assertTrue(langs.contains("en_US"))
        job.cancel()
    }

    @Test
    fun languageError_setsErrorState() = runTest {
        coEvery { appDataRepository.getSupportedLanguages() } throws IOException()
        viewModel = SettingsViewModel(
            appDataRepository, championRepository, getChampionDataUseCase, Dispatchers.Main
        )
        val job = launch(UnconfinedTestDispatcher()) { viewModel.languages.collect() }
        advanceUntilIdle()
        assertTrue(viewModel.languages.value is UiState.Error)
        job.cancel()
    }

    @Test
    fun selectLanguage_savesLanguageClearsDataResetsCache() = runTest {
        getChampionDataUseCase.setVersion(VERSION_14_0)
        coJustRun { appDataRepository.setLanguage(any()) }
        coJustRun { appDataRepository.setLocalVersion(any()) }
        var done = false

        viewModel.selectLanguage("zh_CN") { done = true }
        advanceUntilIdle()

        coVerify { appDataRepository.setLanguage("zh_CN") }
        coVerify { appDataRepository.setLocalVersion("0") }
        assertNull(getChampionDataUseCase.getVersion())
        assertTrue(done)
    }

    @Test
    fun refreshData_clearsDataResetsCache() = runTest {
        getChampionDataUseCase.setVersion(VERSION_14_0)
        coJustRun { appDataRepository.setLocalVersion(any()) }
        var done = false

        viewModel.refreshData { done = true }
        advanceUntilIdle()

        coVerify { appDataRepository.setLocalVersion("0") }
        assertNull(getChampionDataUseCase.getVersion())
        assertTrue(done)
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.settings.SettingsViewModelTest" --console=plain
```

Expected: FAIL — `SettingsViewModel` class does not exist yet.

- [ ] **Step 3: Create `SettingsViewModel.kt`**

Create `app/src/main/java/com/zzy/champions/ui/settings/SettingsViewModel.kt`:

```kotlin
package com.zzy.champions.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.domain.GetChampionDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val championRepository: ChampionRepository,
    private val getChampionDataUseCase: GetChampionDataUseCase,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val currentLanguage: StateFlow<String> = appDataRepository.getLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _languages = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val languages: StateFlow<UiState<List<String>>> = _languages.asStateFlow()

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        viewModelScope.launch(dispatcher) {
            _languages.value = try {
                UiState.Success(appDataRepository.getSupportedLanguages())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e)
            }
        }
    }

    fun selectLanguage(language: String, onDone: () -> Unit) {
        viewModelScope.launch(dispatcher) {
            appDataRepository.setLanguage(language)
            championRepository.clearLocalData()
            appDataRepository.setLocalVersion("0")
            getChampionDataUseCase.reset()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun refreshData(onDone: () -> Unit) {
        viewModelScope.launch(dispatcher) {
            championRepository.clearLocalData()
            appDataRepository.setLocalVersion("0")
            getChampionDataUseCase.reset()
            withContext(Dispatchers.Main) { onDone() }
        }
    }
}
```

- [ ] **Step 4: Run all SettingsViewModel tests**

```bash
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.settings.SettingsViewModelTest" --console=plain
```

Expected: `BUILD SUCCESSFUL`, 4 tests pass.

- [ ] **Step 5: Run all unit tests to confirm no regressions**

```bash
./gradlew :app:testDebugUnitTest --console=plain
```

Expected: `BUILD SUCCESSFUL`, 30 tests pass.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/settings/SettingsViewModel.kt \
        app/src/test/java/com/zzy/champions/settings/SettingsViewModelTest.kt
git commit -m "feat: add SettingsViewModel with language and refresh actions"
```

---

### Task 7: String resources

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add new strings inside `<resources>`**

Add before the closing `</resources>` tag:

```xml
    <string name="switch_language">Switch Language</string>
    <string name="select_language">Select Language</string>
    <string name="refresh_data">Refresh Data</string>
    <string name="confirm">Confirm</string>
    <string name="cancel">Cancel</string>
    <string name="refresh_data_dialog_title">Refresh Data?</string>
    <string name="refresh_data_dialog_message">All local data will be cleared and reloaded from the internet.</string>
    <string name="switch_language_dialog_title">Switch Language?</string>
    <string name="switch_language_dialog_message">Champion data will be reloaded in %1$s.</string>
```

- [ ] **Step 2: Build to confirm resources compile**

```bash
./gradlew :app:generateDebugResources --console=plain
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat: add settings string resources"
```

---

### Task 8: SettingsNavigation.kt — route constants and NavGraphBuilder extensions

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/navigation/SettingsNavigation.kt`

This file wires the settings and language picker destinations. Both `SettingsRoute` and `LanguageRoute` composables are referenced here but defined in Tasks 9 and 10 — build will fail until those are created.

- [ ] **Step 1: Create `SettingsNavigation.kt`**

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
    onBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onRefreshDone: () -> Unit,
) {
    composable(
        route = SETTINGS_ROUTE,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseIn))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseOut))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseIn))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseOut))
        },
    ) {
        SettingsRoute(
            onBack = onBack,
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
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseIn))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300, easing = EaseOut))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseIn))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300, easing = EaseOut))
        },
    ) {
        LanguageRoute(
            onBack = onBack,
            onLanguageSelected = onLanguageSelected,
        )
    }
}
```

Do NOT build yet — `SettingsRoute` and `LanguageRoute` don't exist until Tasks 9 and 10.

---

### Task 9: SettingScreen.kt — settings route composable

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt` _(delete all commented content, replace with the following)_

- [ ] **Step 1: Replace `SettingScreen.kt` entirely**

```kotlin
package com.zzy.champions.ui.settings.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.zzy.champions.R
import com.zzy.champions.ui.settings.SettingsViewModel

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onRefreshDone: () -> Unit,
) {
    var showRefreshDialog by remember { mutableStateOf(false) }

    if (showRefreshDialog) {
        AlertDialog(
            onDismissRequest = { showRefreshDialog = false },
            title = { Text(stringResource(R.string.refresh_data_dialog_title)) },
            text = { Text(stringResource(R.string.refresh_data_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRefreshDialog = false
                    viewModel.refreshData(onDone = onRefreshDone)
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRefreshDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { SettingAppbar(onBack = onBack) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SettingItem(itemName = stringResource(R.string.switch_language)) {
                IconButton(onClick = onLanguageClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
            SettingItem(itemName = stringResource(R.string.refresh_data)) {
                IconButton(onClick = { showRefreshDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
```

---

### Task 10: LanguageScreen.kt — language picker composable

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/settings/compose/LanguageScreen.kt`

- [ ] **Step 1: Create `LanguageScreen.kt`**

```kotlin
package com.zzy.champions.ui.settings.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.R
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.settings.SettingsViewModel

@Composable
fun LanguageRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLanguageSelected: () -> Unit,
) {
    val languages by viewModel.languages.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var pendingLanguage by remember { mutableStateOf<String?>(null) }

    pendingLanguage?.let { lang ->
        AlertDialog(
            onDismissRequest = { pendingLanguage = null },
            title = { Text(stringResource(R.string.switch_language_dialog_title)) },
            text = { Text(stringResource(R.string.switch_language_dialog_message, lang)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingLanguage = null
                    viewModel.selectLanguage(lang, onDone = onLanguageSelected)
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingLanguage = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { LanguageAppbar(onBack = onBack) }
    ) { padding ->
        when (languages) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(stringResource(R.string.internet_connection_error)) }

            is UiState.Success -> LazyColumn(Modifier.padding(padding)) {
                items((languages as UiState.Success).data) { lang ->
                    LanguageItem(
                        language = lang,
                        isSelected = lang == currentLanguage,
                        onClick = { pendingLanguage = lang }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageAppbar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.select_language)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun LanguageItem(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
```

- [ ] **Step 2: Build to confirm Tasks 8–10 compile together**

```bash
./gradlew :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/settings/compose/SettingScreen.kt \
        app/src/main/java/com/zzy/champions/ui/settings/compose/LanguageScreen.kt \
        app/src/main/java/com/zzy/champions/ui/navigation/SettingsNavigation.kt
git commit -m "feat: add settings and language picker screens"
```

---

### Task 11: Wire navigation — ChampionIndexNavigation, ChampionNavHost, ChampionIndexHeader

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexScreen.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexHeader.kt`

#### 11a — ChampionIndexNavigation reads the refresh SavedStateHandle signal

- [ ] **Step 1: Update `ChampionIndexNavigation.kt`**

```kotlin
package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.zzy.champions.data.model.Champion
import com.zzy.champions.ui.index.compose.ChampionIndexRoute

const val CHAMPION_INDEX_ROUTE = "index"

fun NavController.navigateToChampionIndex(navOptions: NavOptions) =
    navigate(CHAMPION_INDEX_ROUTE, navOptions)

fun NavGraphBuilder.championIndexScreen(
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit,
) {
    composable(
        route = CHAMPION_INDEX_ROUTE,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Right
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        }
    ) { backStackEntry ->
        val shouldRefresh by backStackEntry.savedStateHandle
            .getStateFlow("refresh", false)
            .collectAsStateWithLifecycle()

        ChampionIndexRoute(
            onItemClick = onItemClick,
            onSettingClick = onSettingClick,
            shouldRefresh = shouldRefresh,
            onRefreshConsumed = { backStackEntry.savedStateHandle["refresh"] = false },
        )
    }
}
```

#### 11b — ChampionIndexRoute acts on the refresh signal

- [ ] **Step 2: Update `ChampionIndexRoute` in `ChampionIndexScreen.kt`**

Replace `ChampionIndexRoute`:

```kotlin
@Composable
fun ChampionIndexRoute(
    modifier: Modifier = Modifier,
    viewModel: ChampionViewModel = hiltViewModel(),
    onSettingClick: () -> Unit,
    onItemClick: (Champion) -> Unit,
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
) {
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            onRefreshConsumed()
        }
    }

    val champions by viewModel.champions.collectAsStateWithLifecycle()

    ChampionIndexScreen(
        modifier = modifier,
        championsState = champions,
        onUpdateSearchKeyword = viewModel::updateSearchKeyword,
        onInsertBuilds = {},
        onSettingClick = onSettingClick,
        onItemClick = onItemClick,
    )
}
```

Add the missing `LaunchedEffect` import at the top of the file:
```kotlin
import androidx.compose.runtime.LaunchedEffect
```

#### 11c — ChampionNavHost wires settings routes

- [ ] **Step 3: Replace `ChampionNavHost.kt`**

```kotlin
package com.zzy.champions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun ChampionNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = CHAMPION_INDEX_ROUTE
    ) {
        championIndexScreen(
            onItemClick = { navController.navigateToChampionDetail(it.id) },
            onSettingClick = { navController.navigate(SETTINGS_ROUTE) },
        )

        championDetailScreen(onLinkClick)

        settingsScreen(
            onBack = { navController.popBackStack() },
            onLanguageClick = { navController.navigate(LANGUAGE_ROUTE) },
            onRefreshDone = {
                navController.getBackStackEntry(CHAMPION_INDEX_ROUTE)
                    .savedStateHandle["refresh"] = true
                navController.popBackStack()
            }
        )

        languageScreen(
            onBack = { navController.popBackStack() },
            onLanguageSelected = {
                navController.getBackStackEntry(CHAMPION_INDEX_ROUTE)
                    .savedStateHandle["refresh"] = true
                navController.popBackStack(CHAMPION_INDEX_ROUTE, inclusive = false)
            }
        )
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        restoreState = true
    }
```

#### 11d — Uncomment the gear icon in Header

- [ ] **Step 4: Update `ChampionIndexHeader.kt`**

Replace the commented `IconButton` block:

```kotlin
//        IconButton(
//            modifier = Modifier.align(Alignment.TopEnd),
//            onClick = onSettingClick
//        ) {
//            Icon(imageVector = Icons.Default.Settings, contentDescription = "settings", tint = MaterialTheme.colorScheme.tertiary)
//        }
```

With:

```kotlin
IconButton(
    modifier = Modifier.align(Alignment.TopEnd),
    onClick = onSettingClick
) {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "settings",
        tint = MaterialTheme.colorScheme.tertiary
    )
}
```

Also add the missing imports at the top of `ChampionIndexHeader.kt`:

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
```

- [ ] **Step 5: Build the full app**

```bash
./gradlew :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/zzy/champions/ui/navigation/ChampionIndexNavigation.kt \
        app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexScreen.kt \
        app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt \
        app/src/main/java/com/zzy/champions/ui/index/compose/ChampionIndexHeader.kt
git commit -m "feat: wire settings navigation and gear icon entry point"
```

---

### Task 12: Full CI verify

- [ ] **Step 1: Run the full CI check**

```bash
./gradlew --stop; rm -rf app/build/intermediates/lint-cache
./gradlew lint testDebugUnitTest -P roborazzi.test.verify=true --console=plain
```

Expected: `BUILD SUCCESSFUL`, 30 tests pass.

If the screenshot tests fail (Roborazzi pixel diff due to the gear icon appearing in screenshots), re-record them:

```bash
./gradlew recordRoborazziDebug --console=plain
```

Then run the CI check again to confirm.

- [ ] **Step 2: Commit updated screenshots if re-recorded**

```bash
git add app/src/test/screenshots/
git commit -m "chore: update roborazzi screenshots for gear icon"
```

- [ ] **Step 3: Install on emulator and manually verify the golden path**

```bash
./gradlew installDebug
```

Verify:
1. Gear icon appears top-right of the champion index screen.
2. Tapping gear opens Settings screen with two rows.
3. Tapping "Refresh Data" shows the confirmation dialog → Cancel dismisses → Confirm clears data and returns to index showing a loading state, then champions reload.
4. Tapping "Switch Language" opens the language list with a loading spinner, then the list appears with the current language check-marked.
5. Tapping a language shows the confirmation dialog → Cancel dismisses → Confirm returns to index and reloads champions in the new language.
6. Back button from settings/language returns to index without triggering a reload.
