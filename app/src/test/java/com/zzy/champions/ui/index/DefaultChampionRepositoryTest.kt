package com.zzy.champions.ui.index

import com.zzy.champions.data.local.ChampionBuildDao
import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.BUILD_OP_GG
import com.zzy.champions.data.model.BUILD_OP_GG_ARAM
import com.zzy.champions.data.model.BUILD_UGG
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionResponse
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.TestUtil.createChampion
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    private lateinit var cDao: ChampionDao
    @MockK
    private lateinit var cbDao: ChampionBuildDao

    private lateinit var championRepository: DefaultChampionRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        championRepository = DefaultChampionRepository(api, dsManager, cDao, cbDao)
    }

    @Test
    fun preloadDataForFirstOpen_whenItIsFirstOpen() {
        coEvery { dsManager.isFirstOpen() } returns true
        coJustRun { cbDao.addNewBuild(
            BUILD_OP_GG,
            BUILD_UGG,
            BUILD_OP_GG_ARAM) }
        coJustRun { dsManager.setNotFirstOpen() }

        runTest {
            championRepository.preloadDataForFirstOpen()
        }

        coVerify { cbDao.addNewBuild(
            BUILD_OP_GG,
            BUILD_UGG,
            BUILD_OP_GG_ARAM) }
        coVerify { dsManager.setNotFirstOpen() }
    }

    @Test
    fun preloadDataForFirstOpen_whenItIsNotFirstOpen() {
        coEvery { dsManager.isFirstOpen() } returns false
        coJustRun { cbDao.addNewBuild(
            BUILD_OP_GG,
            BUILD_UGG,
            BUILD_OP_GG_ARAM) }
        coJustRun { dsManager.setNotFirstOpen() }

        runTest {
            championRepository.preloadDataForFirstOpen()
        }

        coVerify(exactly = 0) { cbDao.addNewBuild(
            BUILD_OP_GG,
            BUILD_UGG,
            BUILD_OP_GG_ARAM) }
        coVerify(exactly = 0) { dsManager.setNotFirstOpen() }
    }

    @Test
    fun getVersion_whenLocalVersionIsRemoteVersion_localVersionNotUpdate_useRemoteIsFalse() {
        coEvery { api.getVersions() } returns listOf(VERSION_1_0)
        coEvery { dsManager.getVersion() } returns VERSION_1_0
        coJustRun { dsManager.setVersion(VERSION_1_0) }

        runTest {
            championRepository.getVersion()
        }

        coVerify(exactly = 0) { dsManager.setVersion(VERSION_1_0) }

        assertEquals(false, championRepository.getUseRemote())
        assertEquals(Champion.version, VERSION_1_0)
    }

    @Test
    fun getVersion_whenLocalVersionLowerThanRemoteVersion_localVersionUpdate_useRemoteIsTrue() {
        coEvery { api.getVersions() } returns listOf(VERSION_1_1)
        coEvery { dsManager.getVersion() } returns VERSION_1_0
        coJustRun { dsManager.setVersion(VERSION_1_1) }

        runTest {
            championRepository.getVersion()
        }

        coVerify { dsManager.setVersion(VERSION_1_1) }

        assertEquals(true, championRepository.getUseRemote())
        assertEquals(Champion.version, VERSION_1_1)
    }

    @Test
    fun getLocalChampions_When_UseRemoteIsFalse() {
        val aatrox = createChampion("aatrox")
        val ahri = createChampion("ahri")
        coEvery {
            api.getChampions(VERSION_1_0, LANGUAGE_US)
        } returns ChampionResponse(HashMap<String, Champion>().apply { this["aatrox"] = aatrox })

        coJustRun { cDao.insertList(listOf(aatrox)) }
        coEvery { cDao.getAll() } returns listOf(ahri)

        runTest {
            championRepository.setUseRemote(false)

            val result = championRepository.getAllChampions(VERSION_1_0, LANGUAGE_US)

            assertEquals(ahri, result[0])
        }

        assertEquals(false, championRepository.getUseRemote())
        coVerify(exactly = 0) { api.getChampions(VERSION_1_0, LANGUAGE_US) }
        coVerify(exactly = 0) { cDao.insertList(listOf(aatrox)) }
    }

    @Test
    fun getRemoteChampions_When_UseRemoteIsTrue_And_UpdateDataBase() {
        val aatrox = createChampion("aatrox")
        val ahri = createChampion("ahri")

        coEvery { api.getChampions(VERSION_1_0, LANGUAGE_US) } returns ChampionResponse(HashMap<String, Champion>().apply { this["aatrox"] = aatrox })
        coJustRun { cDao.insertList(listOf(aatrox)) }
        coEvery { cDao.getAll() } returns listOf(ahri)

        runTest {
            championRepository.setUseRemote(true)

            val result = championRepository.getAllChampions(VERSION_1_0, LANGUAGE_US)

            assertEquals(aatrox, result[0])
        }

        coVerify { cDao.insertList(listOf(aatrox)) }
    }
}