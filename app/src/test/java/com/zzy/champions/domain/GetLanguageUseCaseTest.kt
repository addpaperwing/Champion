package com.zzy.champions.domain

import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.index.DefaultChampionRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetLanguageUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var championRepository: DefaultChampionRepository

    private lateinit var useCase : GetLanguageUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetLanguageUseCase(championRepository, Dispatchers.Main)
    }

    @Test
    fun getLanguageWhenCachedLanguageIsNull() = runTest {
        //mock result
        val en = "en"

        //Mock return result to mock result
        coEvery { championRepository.getLanguage() } returns en

        //Cached language should be null as default
        assertEquals(
            null,
            useCase.getCachedLanguage()
        )

        //use case return should be same as mock result
        assertEquals(
            en,
            useCase().first()
        )
        //repo's getLanguage function should be run
        coVerify { championRepository.getLanguage() }
        //cached should be not null and equal to mock result
        assertEquals(
            en,
            useCase.getCachedLanguage()
        )
    }

    @Test
    fun getLanguageWhenCachedLanguageIsNotNull() = runTest {
        //mock default language
        val default = "en_a"
        //mock result
        val result = "en_b"

        //Mock return result to mock result
        coEvery { championRepository.getLanguage() } returns result

        //set cached language to mock default language
        useCase.setCachedLanguage(default)

        //use case return should return mock default language
        assertEquals(
            default,
            useCase().first()
        )
        //repo's getLanguage function should be not run
        coVerify(exactly = 0) { championRepository.getLanguage() }

        //cached should be same as mock default language
        assertEquals(
            default,
            useCase.getCachedLanguage()
        )
    }
}