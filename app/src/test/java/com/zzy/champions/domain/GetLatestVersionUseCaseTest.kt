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
import java.io.IOException

private const val LOCAL_VERSION = "local"
private const val REMOTE_VERSION = "remote"
private const val CACHED_VERSION = "cached"

class GetLatestVersionUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var championRepository: DefaultChampionRepository

    private lateinit var useCase : GetLatestVersionUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetLatestVersionUseCase(championRepository, Dispatchers.Main)
    }

    @Test
    fun whenCachedVersionIsNull_getRemoteVersionFailed_returnLocalVersionWithoutCaching() = runTest {
        //Mock get remote version throw exception
        coEvery { championRepository.getRemoteVersion() } throws IOException("error")
        //Mock get local version result
        coEvery { championRepository.getLocalVersion() } returns LOCAL_VERSION

        //Cached version should be null as default
        assertEquals(
            null,
            useCase.getCachedVersion()
        )

        //use case return should be same as local version
        assertEquals(
            LOCAL_VERSION,
            useCase().first()
        )
        //get remote version function should be called
        coVerify { championRepository.getRemoteVersion() }
        //get local version function should be called
        coVerify { championRepository.getLocalVersion() }

        //cached should keep null
        assertEquals(
            null,
            useCase.getCachedVersion()
        )
    }

    @Test
    fun whenCachedVersionIsNull_getRemoteVersionSuccessful_returnRemoteVersionAndCacheResult() = runTest {
        //Mock get remote version result
        coEvery { championRepository.getRemoteVersion() } returns listOf(REMOTE_VERSION)
        //Mock get local version result
        coEvery { championRepository.getLocalVersion() } returns LOCAL_VERSION

        //Cached version should be null as default
        assertEquals(
            null,
            useCase.getCachedVersion()
        )

        //use case return should be same as remote version
        assertEquals(
            REMOTE_VERSION,
            useCase().first()
        )
        //get remote version function should be called
        coVerify { championRepository.getRemoteVersion() }
        //get local version function should not be called
        coVerify(exactly = 0) { championRepository.getLocalVersion() }

        //cached should be same as remote version
        assertEquals(
            REMOTE_VERSION,
            useCase.getCachedVersion()
        )
    }

    @Test
    fun whenCachedVersionIsNotNull_returnCachedVersion() = runTest {
        //Mock get remote version result
        coEvery { championRepository.getRemoteVersion() } returns listOf(REMOTE_VERSION)
        //Mock get local version result
        coEvery { championRepository.getLocalVersion() } returns LOCAL_VERSION

        //Cached version should be null as default
        assertEquals(
            null,
            useCase.getCachedVersion()
        )

        //set cached version
        useCase.setCachedVersion(CACHED_VERSION)

        //use case return should be same as cached version
        assertEquals(
            CACHED_VERSION,
            useCase().first()
        )
        //get remote version function should not be called
        coVerify(exactly = 0) { championRepository.getRemoteVersion() }
        //get local version function should not be called
        coVerify(exactly = 0) { championRepository.getLocalVersion() }

        //cached should be same as remote version
        assertEquals(
            CACHED_VERSION,
            useCase.getCachedVersion()
        )
    }
}