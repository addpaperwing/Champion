package com.zzy.champions.ui.grid

import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionResponse
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.grid.TestUtil.LOCAL_CHAMP_NAME
import com.zzy.champions.ui.grid.TestUtil.REMOTE_CHAMP_NAME
import com.zzy.champions.ui.grid.TestUtil.aatrox
import com.zzy.champions.ui.grid.TestUtil.createChampion
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

        coVerify(exactly = 0) { dsManager.setVersion(VERSION_1_0) }

        assertEquals(false, championRepository.getUseRemote())
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

        assertEquals(true, championRepository.getUseRemote())
        assertEquals(Champion.version, VERSION_1_1)
    }

    @Test
    fun getLocalChampions_When_UseRemote_Is_False() {
        val aatrox = createChampion("aatrox", 1)
        val ahri = createChampion("ahri", 2)
        coEvery {
            api.getChampions(VERSION_1_0, LANGUAGE_US)
        } returns ChampionResponse(HashMap<String, Champion>().apply { this["aatrox"] = aatrox })

        coJustRun { dao.insertList(listOf(aatrox)) }
        coEvery { dao.getAll() } returns listOf(ahri)

        runTest {
            championRepository.setUseRemote(false)

            val result = championRepository.getChampions(VERSION_1_0, LANGUAGE_US)

            assertEquals(ahri, result[0])
        }

        assertEquals(false, championRepository.getUseRemote())
        coVerify(exactly = 0) { api.getChampions(VERSION_1_0, LANGUAGE_US) }
        coVerify(exactly = 0) { dao.insertList(listOf(aatrox)) }
    }

    @Test
    fun getRemoteChampions_When_UseRemote_Is_True_And_UpdateDataBase() {
        val aatrox = createChampion("aatrox", 1)
        val ahri = createChampion("ahri", 2)

        coEvery { api.getChampions(VERSION_1_0, LANGUAGE_US) } returns ChampionResponse(HashMap<String, Champion>().apply { this["aatrox"] = aatrox })
        coJustRun { dao.insertList(listOf(aatrox)) }
        coEvery { dao.getAll() } returns listOf(ahri)

        runTest {
            championRepository.setUseRemote(true)

            val result = championRepository.getChampions(VERSION_1_0, LANGUAGE_US)

            assertEquals(aatrox, result[0])
        }

        coVerify { dao.insertList(listOf(aatrox)) }
    }
}