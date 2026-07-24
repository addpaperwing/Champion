package com.zzy.champions.settings

import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestChampionRepository
import com.zzy.champions.TestItemRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.akali
import com.zzy.champions.data.local.PENDING_VERSION
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.domain.GetChampionDataUseCase
import com.zzy.champions.domain.GetItemDataUseCase
import com.zzy.champions.ui.settings.SettingsViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    private val itemRepository = TestItemRepository()
    private lateinit var getChampionDataUseCase: GetChampionDataUseCase
    private lateinit var getItemDataUseCase: GetItemDataUseCase
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getChampionDataUseCase = GetChampionDataUseCase(
            championRepository, appDataRepository, Dispatchers.Main
        )
        getItemDataUseCase = GetItemDataUseCase(itemRepository, appDataRepository, Dispatchers.Main)
        coEvery { appDataRepository.getLanguage() } returns flowOf("en_US")
        coEvery { appDataRepository.getSupportedLanguages() } returns listOf("en_US", "zh_CN", "ko_KR")
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        justRun { appDataRepository.notifyDataRefreshed() }

        viewModel = SettingsViewModel(
            appDataRepository, championRepository, getChampionDataUseCase, getItemDataUseCase, Dispatchers.Main
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
    fun gameVersionExposesLocalVersion() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.gameVersion.collect() }
        advanceUntilIdle()
        assertEquals(VERSION_14_0, viewModel.gameVersion.value)
        job.cancel()
    }

    @Test
    fun gameVersionHidesPendingSentinel() = runTest {
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(PENDING_VERSION)
        viewModel = SettingsViewModel(
            appDataRepository, championRepository, getChampionDataUseCase, getItemDataUseCase, Dispatchers.Main
        )
        val job = launch(UnconfinedTestDispatcher()) { viewModel.gameVersion.collect() }
        advanceUntilIdle()
        assertEquals("", viewModel.gameVersion.value)
        job.cancel()
    }

    @Test
    fun languageError_setsErrorState() = runTest {
        coEvery { appDataRepository.getSupportedLanguages() } throws IOException()
        viewModel = SettingsViewModel(
            appDataRepository, championRepository, getChampionDataUseCase, getItemDataUseCase, Dispatchers.Main
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
        var done = false

        championRepository.saveLocalChampions(listOf(akali))
        viewModel.selectLanguage("zh_CN") { _ -> done = true }
        advanceUntilIdle()

        coVerify { appDataRepository.setLanguage("zh_CN") }
        assertNull(getChampionDataUseCase.getVersion())
        assertTrue(done)
        assertTrue(championRepository.searchChampionsBy("").isEmpty())
    }

    // A language switch must NOT invalidate the local game version: the version is independent
    // of display language, and invalidating it here would blank the "latest game version" tile
    // on the very screen the user lands back on after switching languages.
    @Test
    fun selectLanguage_doesNotInvalidateLocalVersion() = runTest {
        coJustRun { appDataRepository.setLanguage(any()) }
        var done = false

        viewModel.selectLanguage("zh_CN") { _ -> done = true }
        advanceUntilIdle()

        coVerify(exactly = 0) { appDataRepository.setLocalVersion(PENDING_VERSION) }
        assertTrue(done)
    }

    @Test
    fun selectLanguage_notifiesDataRefreshed() = runTest {
        coJustRun { appDataRepository.setLanguage(any()) }

        viewModel.selectLanguage("zh_CN") { _ -> }
        advanceUntilIdle()

        verify { appDataRepository.notifyDataRefreshed() }
    }

    @Test
    fun refreshData_clearsDataResetsCache() = runTest {
        getChampionDataUseCase.setVersion(VERSION_14_0)
        coJustRun { appDataRepository.setLocalVersion(any()) }
        var done = false

        championRepository.saveLocalChampions(listOf(akali))
        viewModel.refreshData { _ -> done = true }
        advanceUntilIdle()

        coVerify { appDataRepository.setLocalVersion(PENDING_VERSION) }
        verify { appDataRepository.notifyDataRefreshed() }
        assertNull(getChampionDataUseCase.getVersion())
        assertTrue(done)
        assertTrue(championRepository.searchChampionsBy("").isEmpty())
    }
}
