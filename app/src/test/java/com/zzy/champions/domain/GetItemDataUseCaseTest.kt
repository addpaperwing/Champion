package com.zzy.champions.domain

import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.TestItemRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.data.local.PENDING_VERSION
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetItemDataUseCaseTest {

    @MockK private lateinit var appDataRepository: AppDataRepository
    private lateinit var itemRepository: TestItemRepository
    private lateinit var useCase: GetItemDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        coEvery { appDataRepository.getLanguage() } returns flowOf(LANGUAGE_US)
        itemRepository = TestItemRepository()
        useCase = GetItemDataUseCase(itemRepository, appDataRepository, Dispatchers.Unconfined)
    }

    @Test
    fun invoke_whenItemsCached_returnsSuccessWithCachedItems() = runTest {
        // TestItemRepository starts with 4 items seeded (including retiredTrinket)
        // Cached items are returned as-is without filtering, so 4 items are returned
        val result = useCase()

        assertTrue(result is UiState.Success)
        assertEquals(4, (result as UiState.Success).data.size)
    }

    @Test
    fun invoke_whenItemsEmpty_fetchesFromApiAndReturnsSuccess() = runTest {
        itemRepository.saveLocalItems(emptyList())

        val result = useCase()

        assertTrue(result is UiState.Success)
        assertEquals(4, (result as UiState.Success).data.size)  // re-fetched from fake remote; all 4 fixtures are purchasable
    }

    @Test
    fun invoke_whenNetworkFails_andNoCachedItems_returnsError() = runTest {
        itemRepository.saveLocalItems(emptyList())
        itemRepository.shouldThrowOnFetch = true

        val result = useCase()

        assertTrue(result is UiState.Error)
    }

    @Test
    fun invoke_whenLocalVersionIsPending_resolvesRealVersionAndPersistsIt() = runTest {
        // Mirrors what SettingsViewModel.clearAndRefresh() does on every language switch:
        // local items cleared, local version reset to the PENDING_VERSION sentinel.
        itemRepository.saveLocalItems(emptyList())
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(PENDING_VERSION)
        coEvery { appDataRepository.getRemoteVersion() } returns listOf(VERSION_14_0)
        coJustRun { appDataRepository.setLocalVersion(any()) }

        val result = useCase()

        assertTrue(result is UiState.Success)
        assertEquals(VERSION_14_0, itemRepository.lastRequestedVersion)
        coVerify { appDataRepository.setLocalVersion(VERSION_14_0) }
    }
}
