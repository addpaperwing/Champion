package com.zzy.champions.domain

import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.TestItemRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
        // TestItemRepository starts with 3 items seeded
        val result = useCase()

        assertTrue(result is UiState.Success)
        assertEquals(3, (result as UiState.Success).data.size)
    }

    @Test
    fun invoke_whenItemsEmpty_fetchesFromApiAndReturnsSuccess() = runTest {
        itemRepository.saveLocalItems(emptyList())

        val result = useCase()

        assertTrue(result is UiState.Success)
        assertEquals(3, (result as UiState.Success).data.size)  // re-fetched from fake remote
    }

    @Test
    fun invoke_whenNetworkFails_andNoCachedItems_returnsError() = runTest {
        itemRepository.saveLocalItems(emptyList())
        itemRepository.shouldThrowOnFetch = true

        val result = useCase()

        assertTrue(result is UiState.Error)
    }
}
