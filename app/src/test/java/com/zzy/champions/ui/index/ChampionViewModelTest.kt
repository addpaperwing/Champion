package com.zzy.champions.ui.index

import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.index.TestUtil.aatrox
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
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
        } returns listOfChampion

        runTest {
            viewModel.loadChampions()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Error(ioException), viewModel.champions.value)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
            repository.getAllChampions("","")
        } returns listOfChampion

        runTest {
            viewModel.loadChampions()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.champions.value)

            advanceUntilIdle()
            assertEquals(UiState.Error(ioException), viewModel.champions.value)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllChampions_When_getChampions_error() {
        val ioException = IOException("error")
        coEvery { repository.getVersion() } returns ""
        coEvery { repository.getLanguage() } returns ""
        coEvery {
            repository.getAllChampions("","")
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
}