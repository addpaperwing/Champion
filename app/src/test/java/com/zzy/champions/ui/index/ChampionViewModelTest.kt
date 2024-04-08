package com.zzy.champions.ui.index

import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.domain.GetAndSaveChampionBasicDataUseCase
import com.zzy.champions.domain.InsertBuildsForFirstOpenUseCase
import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.TestUtil
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException


private const val VERSION_1_0 = "1.0"
private const val CHAMPION_ID = "aatrox"
private val Champion = TestUtil.createChampion(CHAMPION_ID)
@OptIn(ExperimentalCoroutinesApi::class)
class ChampionViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var insertBuildsForFirstOpenUseCase: InsertBuildsForFirstOpenUseCase
    @MockK
    private lateinit var getAndSaveChampionBasicDataUseCase: GetAndSaveChampionBasicDataUseCase

    private lateinit var viewModel: ChampionViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = ChampionViewModel(insertBuildsForFirstOpenUseCase, getAndSaveChampionBasicDataUseCase)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            UiState.Loading,
            viewModel.champions.value,
        )
    }

    @Test
    fun insertBuildRuns() = runTest {
        coJustRun { insertBuildsForFirstOpenUseCase() }

        viewModel.insertBuildsWhenFirstOpen()

        coVerify { insertBuildsForFirstOpenUseCase() }
    }

    @Test
    fun updateSearchKeyword_championsState_From_LoadingState_To_SuccessState() {
        val champions = listOf(Champion)
        val championData = ChampionData(VERSION_1_0, champions)
        coEvery {
            getAndSaveChampionBasicDataUseCase(any())
        } coAnswers {
            delay(200)
            flowOf(UiState.Success(championData))
        }

        runTest {
            val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.champions.collect() }

            viewModel.updateSearchKeyword("")
            advanceTimeBy( 199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()

            collectJob.cancel()
        }

        assertEquals(UiState.Success(championData), viewModel.champions.value)
    }

   @Test
    fun updateSearchKeyword_championsState_From_LoadingState_To_ErrorState() {
        val ioException = IOException("error")
        coEvery {
            getAndSaveChampionBasicDataUseCase(any())
        } coAnswers {
            delay(200)
            flowOf(UiState.Error(ioException))
        }

        runTest {
            val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.champions.collect() }

            viewModel.updateSearchKeyword("")
            advanceTimeBy( 199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()

            collectJob.cancel()
        }

        assertEquals(UiState.Error(ioException), viewModel.champions.value)
    }


//    @Test
//    fun getChampion_when_error() {
//        val aatroxName = "aatrox"
//        val ioException = IOException("error")
//        coEvery {
//            repository.searchChampionsBy(aatroxName)
//        } coAnswers {
//            delay(200)
//            throw ioException
//        }
//
//        runTest {
//            viewModel.getChampion(aatroxName)
//
//            advanceTimeBy( 199)
//            assertEquals(true, viewModel.champions.value is UiState.Loading)
//
//            advanceUntilIdle()
//        }
//
//        assertEquals(true, viewModel.champions.value is UiState.Error)
//        assertEquals(ioException, (viewModel.champions.value as UiState.Error).exception)
//    }
//
// @Test
//    fun loadChampions_When_getLatestVersion_error() {
//        val listOfChampion = listOf(aatrox())
//        val ioException = IOException("error")
//        coEvery { repository.getVersion() } coAnswers {
//            delay(200)
//            throw ioException
//        }
//        coEvery { repository.getLanguage() } returns ""
//        coEvery {
//            repository.getAllChampions("", "")
//        } coAnswers {
//            listOfChampion
//        }
//
//        runTest {
//            viewModel.loadChampions()
//
//            advanceTimeBy(199)
//            assertEquals(true, viewModel.champions.value is UiState.Loading)
//
//            advanceUntilIdle()
//            assertEquals(true, viewModel.champions.value is UiState.Error)
//            assertEquals(ioException, (viewModel.champions.value as UiState.Error).exception)
//        }
//    }
//
//    @Test
//    fun loadChampions_When_getLanguage_error() {
//        val listOfChampion = listOf(aatrox())
//        val ioException = IOException("error")
//        coEvery { repository.getVersion() } returns ""
//        coEvery { repository.getLanguage() } coAnswers {
//            delay(200)
//            throw ioException
//        }
//        coEvery {
//            repository.getAllChampions("", "")
//        } coAnswers {
//            listOfChampion
//        }
//
//        runTest {
//            viewModel.loadChampions()
//
//            advanceTimeBy(199)
//            assertEquals(true, viewModel.champions.value is UiState.Loading)
//
//            advanceUntilIdle()
//            assertEquals(true, viewModel.champions.value is UiState.Error)
//            assertEquals(ioException, (viewModel.champions.value as UiState.Error).exception)
//        }
//    }
//    @Test
//    fun updatePrediction_successful() {
//        val aatroxName = "aatrox"
//        val champions = listOf(aatrox())
//        coEvery {
//            repository.searchChampionsBy(aatroxName)
//        } coAnswers {
//            champions
//        }
//
//        runTest {
//            viewModel.updatePredictions(aatroxName)
//            advanceUntilIdle()
//            val predictions = viewModel.predictions.first()
//            val championsNames = champions.map { it.name }
//
//            assertEquals(championsNames, predictions)
//        }
//    }
//
//
//    @Test
//    fun updatePrediction_when_error() {
//        val aatroxName = "aatrox"
//        val ioException = IOException("error")
//        coEvery {
//            repository.searchChampionsBy(aatroxName)
//        } coAnswers {
//            throw ioException
//        }
//
//        runTest {
//            viewModel.updatePredictions(aatroxName)
//            advanceUntilIdle()
//            val predictions = viewModel.predictions.first()
//            assertEquals(true, predictions.isEmpty())
//        }
//    }
//
//
//    @Test
//    fun updatePrediction_empty_result_when_query_is_blank() {
//        val query = ""
//
//        runTest {
//            viewModel.updatePredictions(query)
//            advanceUntilIdle()
//            val predictions = viewModel.predictions.first()
//
//            assertEquals(true, predictions.isEmpty())
//        }
//    }
//
//
//    @Test
//    fun clearPrediction__empty_result() {
//        runTest {
//            viewModel.clearPredictions()
//            advanceUntilIdle()
//            val predictions = viewModel.predictions.first()
//
//            assertEquals(true, predictions.isEmpty())
//        }
//    }
}