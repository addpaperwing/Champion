package com.zzy.champions.ui.index

import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.TestUtil.aatrox
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ChampionViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @MockK
    private lateinit var repository: ChampionRepository

    private lateinit var viewModel: ChampionViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = ChampionViewModel(repository, Dispatchers.Main)
    }

    @Test
    fun loadChampions_From_LoadingState_To_SuccessState() {
        val listOfChampion = listOf(aatrox())
        coEvery { repository.getVersion() } returns ""
        coEvery { repository.getLanguage() } returns ""
        coEvery {
            repository.getAllChampions("", "")
        } coAnswers {
            delay(200)
            listOfChampion
        }

        runTest {
            viewModel.loadChampions()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Success(listOfChampion), viewModel.champions.value)
        }
    }

    @Test
    fun loadChampions_When_getLatestVersion_error() {
        val listOfChampion = listOf(aatrox())
        val ioException = IOException("error")
        coEvery { repository.getVersion() } coAnswers {
            delay(200)
            throw ioException
        }
        coEvery { repository.getLanguage() } returns ""
        coEvery {
            repository.getAllChampions("", "")
        } coAnswers {
            listOfChampion
        }

        runTest {
            viewModel.loadChampions()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Error(ioException), viewModel.champions.value)
        }
    }

    @Test
    fun loadChampions_When_getLanguage_error() {
        val listOfChampion = listOf(aatrox())
        val ioException = IOException("error")
        coEvery { repository.getVersion() } returns ""
        coEvery { repository.getLanguage() } coAnswers {
            delay(200)
            throw ioException
        }
        coEvery {
            repository.getAllChampions("", "")
        } coAnswers {
            listOfChampion
        }

        runTest {
            viewModel.loadChampions()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Error(ioException), viewModel.champions.value)
        }
    }

    @Test
    fun loadChampions_When_getAllChampions_error() {
        val ioException = IOException("error")
        coEvery { repository.getVersion() } returns ""
        coEvery { repository.getLanguage() } returns ""
        coEvery {
            repository.getAllChampions("", "")
        } coAnswers {
            delay(200)
            throw ioException
        }

        runTest {
            viewModel.loadChampions()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Error(ioException), viewModel.champions.value)
        }
    }


    @Test
    fun getChampion_From_LoadingState_To_SuccessState() {
        val aatroxName = "aatrox"
        val champions = listOf(aatrox())
        coEvery {
            repository.getChampions(aatroxName)
        } coAnswers {
            delay(200)
            champions
        }

        runTest {
            viewModel.getChampion(aatroxName)

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Success(champions), viewModel.champions.value)
        }
    }


    @Test
    fun getChampion_when_error() {
        val aatroxName = "aatrox"
        val ioException = IOException("error")
        coEvery {
            repository.getChampions(aatroxName)
        } coAnswers {
            delay(200)
            throw ioException
        }

        runTest {
            viewModel.getChampion(aatroxName)

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Error(ioException), viewModel.champions.value)
        }
    }


    @Test
    fun updatePrediction_successful() {
        val aatroxName = "aatrox"
        val champions = listOf(aatrox())
        coEvery {
            repository.getChampions(aatroxName)
        } coAnswers {
            champions
        }

        runTest {
            viewModel.updatePredictions(aatroxName)
            advanceUntilIdle()
            val predictions = viewModel.predictions.first()
            val championsNames = champions.map { it.name }

            assertEquals(championsNames, predictions)
        }
    }


    @Test
    fun updatePrediction_when_error() {
        val aatroxName = "aatrox"
        val ioException = IOException("error")
        coEvery {
            repository.getChampions(aatroxName)
        } coAnswers {
            throw ioException
        }

        runTest {
            viewModel.updatePredictions(aatroxName)
            advanceUntilIdle()
            val predictions = viewModel.predictions.first()
            assertEquals(true, predictions.isEmpty())
        }
    }


    @Test
    fun updatePrediction_empty_result_when_query_is_blank() {
        val query = ""

        runTest {
            viewModel.updatePredictions(query)
            advanceUntilIdle()
            val predictions = viewModel.predictions.first()

            assertEquals(true, predictions.isEmpty())
        }
    }


    @Test
    fun clearPrediction__empty_result() {
        runTest {
            viewModel.clearPredictions()
            advanceUntilIdle()
            val predictions = viewModel.predictions.first()

            assertEquals(true, predictions.isEmpty())
        }
    }
}