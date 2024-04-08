package com.zzy.champions.domain

import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.index.DefaultChampionRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class InsertBuildsForFirstOpenUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var championRepository: DefaultChampionRepository

    private lateinit var useCase : InsertBuildsForFirstOpenUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = InsertBuildsForFirstOpenUseCase(championRepository, Dispatchers.Main)
    }

    @Test
    fun isFirstOpen_runPreloadData_runSetNotFirstOpen() = runTest {
        //Mockk result, always return false when call isFirstOpen
        coEvery { championRepository.isFirstOpen() } returns true
        //Mock these functions will run
        coJustRun { championRepository.preloadDataForFirstOpen() }
        coJustRun { championRepository.setNotFirstOpen() }

        //Call use case
        useCase()

        //Verify the functions are not called
        coVerify { championRepository.preloadDataForFirstOpen() }
        coVerify { championRepository.setNotFirstOpen() }
    }
    @Test
    fun notFirstOpen_doNothing() = runTest {
        //Mockk result, always return false when call isFirstOpen
        coEvery { championRepository.isFirstOpen() } returns false
        //Mock these functions will run
        coJustRun { championRepository.preloadDataForFirstOpen() }
        coJustRun { championRepository.setNotFirstOpen() }

        //Call use case
        useCase()

        //Verify the functions are not called
        coVerify(exactly = 0) { championRepository.preloadDataForFirstOpen() }
        coVerify(exactly = 0) { championRepository.setNotFirstOpen() }
    }
}