package com.zzy.champions.ui.grid

import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionResponse
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.grid.TestUtil.LOCAL_CHAMP_NAME
import com.zzy.champions.ui.grid.TestUtil.REMOTE_CHAMP_NAME
import com.zzy.champions.ui.grid.TestUtil.aatrox
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val VERSION_1_0 = "1.0"
private const val VERSION_1_1 = "1.1"
private const val LANGUAGE_US = "US"

class DefaultChampionRepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var api: Api
    @MockK
    private lateinit var dsManager: DataStoreManager
    @MockK
    private lateinit var dao: ChampionDao

    private lateinit var championRepository: DefaultChampionRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        championRepository = DefaultChampionRepository(api, dsManager, dao)
    }

    @Test
    fun currentVersion_Is_latestVersion_useRemoteFalse_updateChampionCompanionObject() {
        coEvery { api.getVersions() } returns listOf(VERSION_1_0)
        coEvery { dsManager.getVersion() } returns VERSION_1_0
        coJustRun { dsManager.setVersion(VERSION_1_0) }

        runTest {
            championRepository.getLatestVersion()
        }

        coVerify { dsManager.setVersion(VERSION_1_0) wasNot Called }

        assertEquals(championRepository.getUseRemote(), false)
        assertEquals(Champion.version, VERSION_1_0)
    }

    @Test
    fun currentVersion_Is_Not_latestVersion_updateCurrentVersion_useRemoteTrue_updateChampionCompanionObject() {
        coEvery { api.getVersions() } returns listOf(VERSION_1_1)
        coEvery { dsManager.getVersion() } returns VERSION_1_0
        coJustRun { dsManager.setVersion(VERSION_1_1) }

        runTest {
            championRepository.getLatestVersion()
        }

        coVerify { dsManager.setVersion(VERSION_1_1) }

        assertEquals(championRepository.getUseRemote(), true)
        assertEquals(Champion.version, VERSION_1_1)
    }

    @Test
    fun getLocalChampions_When_UseRemote_Is_False() {
        championRepository.setUseRemote(false)

        coEvery { api.getChampions(VERSION_1_0, LANGUAGE_US) } returns ChampionResponse(HashMap<String, Champion>().apply { this["Aatrox"] = aatrox(true) })
        coJustRun { dao.insertList(listOf(aatrox(true))) }
        coEvery { dao.getAll() } returns listOf(aatrox(remote = false))

        runTest {
            val result = championRepository.getChampions(VERSION_1_0, LANGUAGE_US)

            assertEquals(result[0].name, LOCAL_CHAMP_NAME)
        }

        coVerify { dao.insertList(listOf(aatrox(true)))  wasNot Called }
    }

    @Test
    fun getRemoteChampions_When_UseRemote_Is_True_And_UpdateDataBase() {
        championRepository.setUseRemote(true)

        coEvery { api.getChampions(VERSION_1_0, LANGUAGE_US) } returns ChampionResponse(HashMap<String, Champion>().apply { this["Aatrox"] = aatrox(true) })
        coJustRun { dao.insertList(listOf(aatrox(true))) }
        coEvery { dao.getAll() } returns listOf(aatrox(remote = false))

        runTest {
            val result = championRepository.getChampions(VERSION_1_0, LANGUAGE_US)

            assertEquals(result[0].name, REMOTE_CHAMP_NAME)
        }

        coVerify { dao.insertList(listOf(aatrox(true))) }
    }
}