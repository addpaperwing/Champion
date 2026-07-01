package com.zzy.champions.settings

import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestChampionRepository
import com.zzy.champions.TestItemRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.akali
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
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
        coJustRun { appDataRepository.setLocalVersion(any()) }
        var done = false

        championRepository.saveLocalChampions(listOf(akali))
        viewModel.selectLanguage("zh_CN") { _ -> done = true }
        advanceUntilIdle()

        coVerify { appDataRepository.setLanguage("zh_CN") }
        coVerify { appDataRepository.setLocalVersion("0") }
        assertNull(getChampionDataUseCase.getVersion())
        assertTrue(done)
        assertTrue(championRepository.searchChampionsBy("").isEmpty())
    }

    @Test
    fun refreshData_clearsDataResetsCache() = runTest {
        getChampionDataUseCase.setVersion(VERSION_14_0)
        coJustRun { appDataRepository.setLocalVersion(any()) }
        var done = false

        championRepository.saveLocalChampions(listOf(akali))
        viewModel.refreshData { _ -> done = true }
        advanceUntilIdle()

        coVerify { appDataRepository.setLocalVersion("0") }
        assertNull(getChampionDataUseCase.getVersion())
        assertTrue(done)
        assertTrue(championRepository.searchChampionsBy("").isEmpty())
    }
}
