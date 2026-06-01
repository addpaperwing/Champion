# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Android app that views League of Legends champion data, built with Kotlin and Jetpack Compose. Data comes from Riot's public Data Dragon CDN (`https://ddragon.leagueoflegends.com/`).

## Commands

Use `gradlew.bat` on Windows (`./gradlew` on the CI's Ubuntu runner).

- **Build debug APK:** `./gradlew assembleDebug`
- **Lint:** `./gradlew lint`
- **Unit tests:** `./gradlew testDebugUnitTest`
- **Single unit test class:** `./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.detail.ChampionDetailViewModelTest"`
- **Single test method:** append `.methodName` to the `--tests` filter.
- **Instrumentation tests (needs emulator/device):** `./gradlew connectedCheck`
- **Full CI check locally:** `./gradlew lint testDebugUnitTest -P roborazzi.test.verify=true` (this is exactly what CI runs)

### Screenshot tests (Roborazzi)

Screenshot tests live in `app/src/test/` (run on the JVM via Robolectric, not on a device). Reference PNGs are in `app/src/test/screenshots/`.

- **Verify against recorded images:** `./gradlew verifyRoborazziDebug` (or pass `-P roborazzi.test.verify=true` to `testDebugUnitTest`)
- **Record/update reference images** after intentional UI changes: `./gradlew recordRoborazziDebug`

## Architecture

Two Gradle modules: `:app` (everything) and `:network` (a near-empty library scaffold — networking currently lives in `:app`, not here).

Standard Google "recommended app architecture" layering, all under `com.zzy.champions`:

- **`data/remote`** — Retrofit `Api` interface hitting Data Dragon endpoints; Moshi for JSON (with a custom `BigDecimalAdapter`). `UiState<T>` is the sealed wrapper (`Loading` / `Success` / `Error`) the UI observes.
- **`data/local`** — Room database (`ChampionDataBase`, DAOs) for caching champions/details, plus `DataStoreManager` (Preferences DataStore) for app settings like the stored data version and language.
- **`data/repository`** — `*Repository` interfaces with `Default*Repository` implementations, bound via Hilt `@Binds` in `di/RepositoryModule`. Repositories abstract over remote (`Api`) + local (`Dao`/DataStore).
- **`domain`** — Use cases (`GetChampionDataUseCase`, `GetChampionDetailUseCase`) holding business logic. **Key flow:** `GetChampionDataUseCase` compares the locally stored version (DataStore) against the remote version list; if the remote is newer it fetches champions, writes them to Room, and updates the stored version; otherwise it serves the Room cache. The resolved version is cached in-memory after the first call. Network failures fall back to local data (or `DEFAULT_EARLIEST_VERSION`).
- **`ui`** — Compose screens organized by feature (`index`, `detail`, `settings`), each with a `@HiltViewModel`. ViewModels expose `StateFlow<UiState<…>>`. Navigation is Compose Navigation; routes/destinations are defined in `ui/navigation`.

### Dependency injection

Hilt throughout. `ChampionsApplication` is `@HiltAndroidApp`; `MainActivity` is `@AndroidEntryPoint`. Modules in `di/`:
- `NetworkModule` — OkHttp, Retrofit/Moshi, `Api`, and the IO `CoroutineDispatcher`.
- `PersistenceModule` — Room database + DAOs and the DataStore-backed `AppDataSource`. Note: it **pre-populates the `ChampionBuild` table** (op.gg / u.gg links) via a `RoomDatabase.Callback` on first create.
- `RepositoryModule` — `@Binds` interface-to-impl bindings.

The injected `CoroutineDispatcher` (IO) is passed into repositories and use cases for `withContext`, and is swapped for a test dispatcher in unit tests.

## Code generation

KSP (not kapt) drives codegen for Hilt, Room, and Moshi. After changing `@Entity`/DAO, Hilt modules, or Moshi `@JsonClass` models, a rebuild is needed for generated code to update.

The project is on **Kotlin 2.x**, so Compose uses the standalone Compose Compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`) — there is no `composeOptions { kotlinCompilerExtensionVersion }` block anymore.

Image loading is **Coil 3** (`coil3.*` packages; `coil-compose` + `coil-network-okhttp` artifacts). **AGP is intentionally pinned to 8.9.2** — AGP 8.10.x breaks `lintAnalyzeDebug` on Windows with a `RuntimeIssueRegistry*.jar` file-lock (`FileSystemException ... used by another process`). Don't bump AGP without re-checking that `./gradlew lint` runs on Windows.

## Testing conventions

- Unit tests use JUnit4 + MockK. `MainCoroutineRule` swaps `Dispatchers.Main`; `TestChampionRepository` is a fake repo for ViewModel tests.
- Build config: `minSdk` 21, `compile`/`targetSdk` 36, Java/JVM target 17.
