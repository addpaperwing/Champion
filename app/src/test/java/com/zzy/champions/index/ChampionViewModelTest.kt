package com.zzy.champions.index

import com.zzy.champions.ERROR_VERSION
import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestChampionRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.VERSION_14_1
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.domain.DEFAULT_EARLIEST_VERSION
import com.zzy.champions.domain.GetChampionDataUseCase
import com.zzy.champions.ui.index.ChampionViewModel
import com.zzy.champions.ui.theme.ASSASSIN
import com.zzy.champions.ui.theme.MAGE
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
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var appDataRepository: AppDataRepository

    private var championRepository: ChampionRepository = TestChampionRepository()

    private lateinit var getChampionDataUseCase: GetChampionDataUseCase

    private lateinit var viewModel: ChampionViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appDataRepository.getLanguage() } returns flowOf(LANGUAGE_US)

        getChampionDataUseCase = GetChampionDataUseCase(championRepository, appDataRepository, Dispatchers.Main)
        viewModel = ChampionViewModel(getChampionDataUseCase)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            UiState.Loading,
            viewModel.champions.value,
        )
    }

    @Test
    //Different response cases of local and remote data will not cause any other scenario when cached is not null
    //Cached will always be NULL in test cases after this one
    fun championDataUseLocalData_WhenCachedVersionIsNoNull() = runTest {
        //set cached version to 14.0
        getChampionDataUseCase.setVersion(VERSION_14_0)

        //All version use cases will not run, no need to mock
//        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_1_0)
//        coEvery { appDataRepository.getRemoteVersion() } returns listOf(VERSION_1_1)
//        coJustRun { appDataRepository.setLocalVersion(any()) }

        val collectJob1 = launch { viewModel.champions.collect() }

        //Debounce
        advanceUntilIdle()

        assertEquals(UiState.Success(
            ChampionData(
                version = VERSION_14_0,
                champions = emptyList() //Test data for local not set
            )
        ), viewModel.champions.value)

        collectJob1.cancel()
    }


    //local version champion (always be updated at the same time)
    //remote version
    //remote champion
    @Test
    fun dataWillBeRemoteData_localIsEmpty_remoteVersionIsSuccessful_remoteChampionIsSuccessful() = runTest {
        //Memory cached version is null
        assertEquals(null, getChampionDataUseCase.getVersion())

        //Mock return local version is 0 (default)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf("0")
        //Mock return remote version to 14.1
        coEvery { appDataRepository.getRemoteVersion() } returns listOf(VERSION_14_1)

        coJustRun { appDataRepository.setLocalVersion(any()) }

        val collectJob1 = launch { viewModel.champions.collect() }

        advanceUntilIdle()

        //Memory cached version up to date
        assertEquals(VERSION_14_1, getChampionDataUseCase.getVersion())
        //Local version update to date
        coVerify { appDataRepository.setLocalVersion(VERSION_14_1) }

        //Data version is 14.1
        assertEquals(UiState.Success(ChampionData(
            version = VERSION_14_1,
            champions = listOf(Champion(
                            id = "Ahri",
                            name = "Ahri",
                            title = "the Nine-Tailed Fox",
                            info = Info(attack = 3, defense = 4, magic = 8, difficulty = 5),
                            image = Image("Ahri.png"),
                            tags = listOf(MAGE, ASSASSIN),
                            partype = "Mana",
                            stats = Stats()
                        ))
        )), viewModel.champions.value)

        collectJob1.cancel()
    }

    @Test
    fun dataWillBeLocal_localIsSameAsRemote_remoteVersionIsSuccessful_remoteChampionIsSuccessful() = runTest {
        //Memory cached version is null
        assertEquals(null, getChampionDataUseCase.getVersion())

        //Mock return local version is 0 (default)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_1)
        //Mock return remote version to 14.1
        coEvery { appDataRepository.getRemoteVersion() } returns listOf(VERSION_14_1)

        coJustRun { appDataRepository.setLocalVersion(any()) }

        val collectJob1 = launch { viewModel.champions.collect() }

        advanceUntilIdle()

        //Memory cached version up to date
        assertEquals(VERSION_14_1, getChampionDataUseCase.getVersion())
        //Local version update to date
        coVerify(exactly = 0) { appDataRepository.setLocalVersion(VERSION_14_1) }

        //Data version is 14.1
        assertEquals(UiState.Success(ChampionData(
            version = VERSION_14_1,
            champions = emptyList() //Test data for local not set
        )), viewModel.champions.value)

        collectJob1.cancel()
    }


    @Test
    fun dataWillBeDefaultEarliestVersionData_localIsEmpty_remoteVersionIsFailed_remoteChampionIsSuccessful() = runTest {
        //Memory cached version is null
        assertEquals(null, getChampionDataUseCase.getVersion())

        //Mock return local version is 0 (default)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf("0")
        //Mock return remote failed, return default earliest version
        coEvery { appDataRepository.getRemoteVersion() } throws IOException()

        coJustRun { appDataRepository.setLocalVersion(any()) }

        val collectJob1 = launch { viewModel.champions.collect() }

        advanceUntilIdle()

        //Memory cached version will be set to default earliest version
        assertEquals(DEFAULT_EARLIEST_VERSION, getChampionDataUseCase.getVersion())
        //Update local version will not be invoked
        coVerify { appDataRepository.setLocalVersion(DEFAULT_EARLIEST_VERSION) }

        //Data version is default earliest version
        assertEquals(
            UiState.Success(
                ChampionData(
                    version = DEFAULT_EARLIEST_VERSION,
                    champions = listOf(
                        Champion(
                            id = "Akali",
                            name = "Akali",
                            title = "the Rogue Assassin",
                            info = Info(attack = 5, defense = 3, magic = 8, difficulty = 7),
                            image = Image("Akali.png"),
                            tags = listOf(ASSASSIN),
                            partype = "Energy",
                            stats = Stats()
                        )
                    )
                )
            ), viewModel.champions.value
        )

        collectJob1.cancel()
    }

    @Test
    fun dataWillBeLocal_localIsNotEmpty_remoteVersionIsFailed_remoteChampionIsSuccessful() = runTest {
        //Memory cached version is null
        assertEquals(null, getChampionDataUseCase.getVersion())

        //Mock return local version is 14.0
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        //Mock return remote failed, return default earliest version
        coEvery { appDataRepository.getRemoteVersion() } throws IOException()

        coJustRun { appDataRepository.setLocalVersion(any()) }

        val collectJob1 = launch { viewModel.champions.collect() }

        advanceUntilIdle()

        //Memory cached version will be update
        assertEquals(VERSION_14_0, getChampionDataUseCase.getVersion())
        //Update local version will not be invoked
        coVerify(exactly = 0) { appDataRepository.setLocalVersion(any()) }

        //Data version is default earliest version
        assertEquals(
            UiState.Success(
                ChampionData(
                    version = VERSION_14_0,
                    champions = emptyList() //Test data for local not set
                )
            ), viewModel.champions.value
        )

        collectJob1.cancel()
    }


    @Test
    //Different cases of remote version and local data will not cause any other scenario when remote champion is failed
    fun dataWillBeLocal_remoteChampionIsFailed() = runTest {
        //Memory cached version is null
        assertEquals(null, getChampionDataUseCase.getVersion())

        //Mock return local version is 0 (default)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf("0")
        //Mock return remote failed, return default earliest version
        coEvery { appDataRepository.getRemoteVersion() } returns listOf(ERROR_VERSION)

        coJustRun { appDataRepository.setLocalVersion(any()) }

        val collectJob1 = launch { viewModel.champions.collect() }

        advanceUntilIdle()

        //Memory cached version will not be set to default earliest version
        assertEquals(null, getChampionDataUseCase.getVersion())
        //Update local version will not be invoked
        coVerify(exactly = 0) { appDataRepository.setLocalVersion(any()) }

        //Data will be local data
        assertEquals(
            UiState.Success(
                ChampionData(
                    version = "0",
                    champions = emptyList()
                )
            ), viewModel.champions.value
        )

        collectJob1.cancel()
    }

}