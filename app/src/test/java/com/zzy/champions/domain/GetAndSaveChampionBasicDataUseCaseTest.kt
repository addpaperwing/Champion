package com.zzy.champions.domain

import com.zzy.champions.data.model.ChampionResponse
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.TestUtil
import com.zzy.champions.ui.index.DefaultChampionRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException


private const val VERSION_1_0 = "1.0"
private const val VERSION_1_1 = "1.1"
private const val LANGUAGE_US = "US"

private const val REMOTE_CHAMPION_ID = "aatrox"
private val remoteChampion = TestUtil.createChampion(REMOTE_CHAMPION_ID)

private const val LOCAL_CHAMPION_ID = "ahri"
private val localChampion = TestUtil.createChampion(LOCAL_CHAMPION_ID)


class GetAndSaveChampionBasicDataUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var repository: DefaultChampionRepository
    @MockK
    private lateinit var getLatestVersionUseCase: GetLatestVersionUseCase
    @MockK
    private lateinit var getLanguageUseCase: GetLanguageUseCase


    private lateinit var useCase : GetAndSaveChampionBasicDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetAndSaveChampionBasicDataUseCase(repository, Dispatchers.Main, getLatestVersionUseCase, getLanguageUseCase)
    }

    @Test
    fun whenLocalVersionNotEqualsToLatestVersion_getRemoteChampionSuccessful_saveData_returnQueryResult() = runTest {
        //Mock return language to us
        coEvery { getLanguageUseCase() } returns flowOf(LANGUAGE_US)
        //Mock return remote version to 1.1
        coEvery { getLatestVersionUseCase() } returns flowOf(VERSION_1_1)

        //Mock return local version to 1.0
        coEvery { repository.getLocalVersion() } returns VERSION_1_0
        //Mock only version = 1.1 language = us return remote champion
        coEvery { repository.getRemoteChampions(VERSION_1_1, LANGUAGE_US) } returns ChampionResponse(mapOf(REMOTE_CHAMPION_ID to remoteChampion))
        //Mock save local champions call
        coJustRun { repository.saveLocalChampions(VERSION_1_1, listOf(remoteChampion)) }
        //Mock query champion call
        coEvery { repository.searchChampionsBy(any()) } returns emptyList()

        //Default is true
        assertEquals(
            true,
            useCase.localVersionIsNotLatest()
        )
        //remote version should be return as result
        assertEquals(
            VERSION_1_1,
            (useCase("").first() as UiState.Success).data.version
        )
        //get remote champion should be called and input exactly same parameters as mock
        coVerify { repository.getRemoteChampions(VERSION_1_1, LANGUAGE_US) }
        //save local champion should be called and input exactly same parameters as mock
        coVerify { repository.saveLocalChampions(VERSION_1_1, listOf(remoteChampion)) }
        //query champion should be call
        coVerify { repository.searchChampionsBy(any()) }
        //local version will be set and should be same as remote version
        assertEquals(
            false,
            useCase.localVersionIsNotLatest()
        )
    }

    @Test
    fun whenLocalVersionEqualsToLatestVersion_notDoGetAndSaveRemoteChampionDataToLocal_returnQueryResult() = runTest {
        //Mock return language to us
        coEvery { getLanguageUseCase() } returns flowOf(LANGUAGE_US)
        //Mock return remote version to 1.1
        coEvery { getLatestVersionUseCase() } returns flowOf(VERSION_1_1)

        //Mock return local version to 1.1
        coEvery { repository.getLocalVersion() } returns VERSION_1_1
        //Mock get remote champions call
        coEvery { repository.getRemoteChampions(any(), any()) } returns ChampionResponse(emptyMap())
        //Mock save local champions call
        coJustRun { repository.saveLocalChampions(any(), any()) }
        //Mock query champion call
        coEvery { repository.searchChampionsBy(any()) } returns emptyList()

        //Default is true
        assertEquals(
            true,
            useCase.localVersionIsNotLatest()
        )
        //version should be returned as same as remote and local version
        assertEquals(
            VERSION_1_1,
            (useCase("").first() as UiState.Success).data.version
        )
        //get remote champion should not be called
        coVerify(exactly = 0) { repository.getRemoteChampions(any(), any()) }
        //save local champion should not be called
        coVerify(exactly = 0) { repository.saveLocalChampions(any(), any()) }

        //query champion should be call
        coVerify { repository.searchChampionsBy(any()) }
        //localVersionIsNotLatest will be set to false
        assertEquals(
            false,
            useCase.localVersionIsNotLatest()
        )
    }

    @Test
    fun whenLocalVersionNotEqualsToLatestVersion_getRemoteChampionFailed_doNotSaveData_returnQueryResult() = runTest {
        //Mock return language to us
        coEvery { getLanguageUseCase() } returns flowOf(LANGUAGE_US)
        //Mock return remote version to 1.1
        coEvery { getLatestVersionUseCase() } returns flowOf(VERSION_1_1)

        //Mock return local version to 1.0
        coEvery { repository.getLocalVersion() } returns VERSION_1_0
        //Mock get remote data error
        coEvery { repository.getRemoteChampions(any(), any()) } throws IOException("error")
        //Mock save local champions call
        coJustRun { repository.saveLocalChampions(any(), any()) }
        //Mock query champion call
        coEvery { repository.searchChampionsBy(any()) } returns emptyList()

        //Default is true
        assertEquals(
            true,
            useCase.localVersionIsNotLatest()
        )
        //local version should be return as result
        assertEquals(
            VERSION_1_0,
            (useCase("").first() as UiState.Success).data.version
        )
        //get remote champion should be called
        coVerify { repository.getRemoteChampions(any(), any()) }
        //save local champion should not be called
        coVerify(exactly = 0) { repository.saveLocalChampions(any(), any()) }
        //query champion should be call
        coVerify { repository.searchChampionsBy(any()) }

        //localVersionIsNotLatest will keep true
        assertEquals(
            true,
            useCase.localVersionIsNotLatest()
        )
    }
}