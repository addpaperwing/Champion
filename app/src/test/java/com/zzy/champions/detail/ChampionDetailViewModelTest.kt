package com.zzy.champions.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestChampionRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.akali
import com.zzy.champions.akaliDetail
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.NAME_OF_BUILD_OPGG
import com.zzy.champions.data.model.URL_OF_OPGG
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionBuildRepository
import com.zzy.champions.domain.GetChampionDetailUseCase
import com.zzy.champions.ui.navigation.CHAMPION_ID
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ChampionDetailViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var appDataRepository: AppDataRepository

    @MockK
    private lateinit var championBuildRepository: ChampionBuildRepository

    private val championRepository = TestChampionRepository()

    private val savedStateHandle = SavedStateHandle()

    private lateinit var viewModel: ChampionDetailViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        coEvery { championBuildRepository.getBuilds() } returns flowOf(listOf(ChampionBuild(NAME_OF_BUILD_OPGG, URL_OF_OPGG)))
        coEvery { appDataRepository.getLanguage() } returns flowOf(LANGUAGE_US)

        viewModel = ChampionDetailViewModel(
            savedStateHandle,
            appDataRepository,
            championRepository,
            championBuildRepository,
            GetChampionDetailUseCase(championRepository, appDataRepository, Dispatchers.Main)
        )


    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            UiState.Loading,
            viewModel.result.value,
        )
    }

    @Test
    fun versionLoaded() = runTest {
        val collectJob1 = launch(UnconfinedTestDispatcher()) { viewModel.version.collect() }

        assertEquals(
            VERSION_14_0,
            viewModel.version.value,
        )

        collectJob1.cancel()
    }

    @Test
    fun championBuildsLoaded() = runTest {
        assertEquals(
            true,
            viewModel.builds.value.isEmpty(),
        )

        val collectJob1 = launch(UnconfinedTestDispatcher()) { viewModel.builds.collect() }

        assertEquals(
            NAME_OF_BUILD_OPGG,
            viewModel.builds.value[0].nameOfBuild,
        )

        collectJob1.cancel()
    }

    //id blank -> not found -> ui state error
//local detail null -> get remote detail -> save remote detail -> ui state success
//local detail not null -> ui state success

    @Test
    fun errorWhenChampionIdIsBlank() = runTest {
        savedStateHandle[CHAMPION_ID] = ""

        val collectJob1 = launch(UnconfinedTestDispatcher()) { viewModel.result.collect() }

        assertTrue(viewModel.result.value is UiState.Error)
        assertTrue((viewModel.result.value as UiState.Error).exception is IOException)

        collectJob1.cancel()
    }

    @Test
    fun updateLocalWhenLocalDetailIsNull() = runTest {
        savedStateHandle[CHAMPION_ID] = "Akali"
        assertEquals(null, championRepository.getLocalChampionDetail("Akali"))

        val collectJob1 = launch(UnconfinedTestDispatcher()) { viewModel.result.collect() }

        assertEquals(UiState.Success(ChampionAndDetail(akali, akaliDetail)), viewModel.result.value)

        collectJob1.cancel()
    }

    @Test
    fun useLocalDataWhenLocalIsNotNull() = runTest {
        savedStateHandle[CHAMPION_ID] = "Akali"
        championRepository.saveChampionDetail(akaliDetail)

        val collectJob1 = launch(UnconfinedTestDispatcher()) { viewModel.result.collect() }

        assertEquals(UiState.Success(ChampionAndDetail(akali, akaliDetail)), viewModel.result.value)

        collectJob1.cancel()
    }
}