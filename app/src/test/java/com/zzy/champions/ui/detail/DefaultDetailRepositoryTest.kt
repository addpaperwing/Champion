package com.zzy.champions.ui.detail

import com.zzy.champions.data.local.LocalDataSource
import com.zzy.champions.data.local.db.ChampionDatabaseHelper
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetailResponse
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.TestUtil.createChampion
import com.zzy.champions.ui.TestUtil.createChampionDetail
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
import java.io.IOException

private const val VERSION_1_0 = "1.0"
private const val LANGUAGE_US = "US"
class DefaultDetailRepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var api: Api
    @MockK
    private lateinit var localDataSource: LocalDataSource
    @MockK
    private lateinit var dbHelper: ChampionDatabaseHelper

    private lateinit var repository: DefaultDetailRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = DefaultDetailRepository(api, localDataSource, dbHelper)
    }

    @Test
    fun getBasicAndDetail_WhenLocalIsNotNull() {
        val id = "aatrox"
        val aatorx = createChampion(id)
        val remote = createChampionDetail("aatrox_remote")
        val local = createChampionDetail("aatrox_local")

        coEvery { dbHelper.getChampionDetail(id) } returns local
        coEvery { localDataSource.getVersion() } returns VERSION_1_0
        coEvery { localDataSource.getLanguage() } returns LANGUAGE_US
        coEvery { api.getChampionDetail(VERSION_1_0, LANGUAGE_US, id) } returns ChampionDetailResponse(data = mapOf(
            Pair(id, remote)
        ))
        coJustRun { dbHelper.updateChampionDetailData(remote) }

        coEvery { dbHelper.getChampionBasicAndDetailData(id) } returns ChampionAndDetail(aatorx, local)

        runTest {
            val result = repository.getChampionAndDetail(id)
            assertEquals(aatorx, result.champion)
            assertEquals(local, result.detail)
        }

        coVerify { dbHelper.getChampionDetail(id) }
        coVerify(exactly = 0) { localDataSource.getVersion() }
        coVerify(exactly = 0) { localDataSource.getLanguage() }
        coVerify(exactly = 0) { api.getChampionDetail(VERSION_1_0, LANGUAGE_US, id) }
        coVerify(exactly = 0) { dbHelper.updateChampionDetailData(remote) }
    }

    @Test
    fun getBasicAndDetail_WhenLocalIsNull() {
        val id = "aatrox"
        val aatorx = createChampion(id)
        val remote = createChampionDetail("aatrox_remote")

        coEvery { dbHelper.getChampionDetail(id) } returns null
        coEvery { localDataSource.getVersion() } returns VERSION_1_0
        coEvery { localDataSource.getLanguage() } returns LANGUAGE_US
        coEvery { api.getChampionDetail(VERSION_1_0, LANGUAGE_US, id) } returns ChampionDetailResponse(data = mapOf(
            Pair(id, remote)
        ))
        coJustRun { dbHelper.updateChampionDetailData(remote) }

        coEvery { dbHelper.getChampionBasicAndDetailData(id) } returns ChampionAndDetail(aatorx, remote)

        runTest {
            val result = repository.getChampionAndDetail(id)
            assertEquals(aatorx, result.champion)
            assertEquals(remote, result.detail)
        }

        coVerify { dbHelper.getChampionDetail(id) }
        coVerify { localDataSource.getVersion() }
        coVerify { localDataSource.getLanguage() }
        coVerify { api.getChampionDetail(VERSION_1_0, LANGUAGE_US, id) }
        coVerify { dbHelper.updateChampionDetailData(remote) }
    }

    @Test
    fun getBasicAndDetail_WhenLocalIsNull_RemoteIsError() {
        val id = "aatrox"
        val aatorx = createChampion(id)
        val remote = createChampionDetail("aatrox_remote")
        val exception = IOException("error")

        coEvery { dbHelper.getChampionDetail(id) } returns null
        coEvery { localDataSource.getVersion() } returns VERSION_1_0
        coEvery { localDataSource.getLanguage() } returns LANGUAGE_US
        coEvery { api.getChampionDetail(VERSION_1_0, LANGUAGE_US, id) } throws exception
        coJustRun { dbHelper.updateChampionDetailData(remote) }

        coEvery { dbHelper.getChampionBasicAndDetailData(id) } returns ChampionAndDetail(aatorx, remote)

        runTest {
            try {
                repository.getChampionAndDetail(id)
            } catch (e: Throwable) {
                assertEquals(e, exception)
            }
        }

        coVerify { dbHelper.getChampionDetail(id) }
        coVerify { localDataSource.getVersion() }
        coVerify { localDataSource.getLanguage() }
        coVerify { api.getChampionDetail(VERSION_1_0, LANGUAGE_US, id) }
        coVerify(exactly = 0) { dbHelper.updateChampionDetailData(remote) }
        coVerify(exactly = 0) { dbHelper.getChampionBasicAndDetailData(id) }
    }
}