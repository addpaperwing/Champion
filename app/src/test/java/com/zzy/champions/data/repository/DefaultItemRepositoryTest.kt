package com.zzy.champions.data.repository

import com.zzy.champions.data.local.db.ItemDao
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.data.model.ItemResponse
import com.zzy.champions.data.remote.Api
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DefaultItemRepositoryTest {

    @MockK private lateinit var api: Api
    @MockK private lateinit var dao: ItemDao
    private lateinit var repository: DefaultItemRepository

    private val fakeItemFromApi = Item(
        id = "",   // no id in JSON body — set from map key
        name = "Long Sword",
        description = "A sword",
        plaintext = "Slightly increases Attack Damage",
        image = Image("1036.png"),
        gold = ItemGold(base = 350, purchasable = true, total = 350, sell = 245),
        tags = listOf("Damage"),
        maps = mapOf("11" to true),
        stats = mapOf("FlatPhysicalDamageMod" to 10.0),
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = DefaultItemRepository(api, dao, Dispatchers.Unconfined)
    }

    @Test
    fun getRemoteItems_setsIdFromMapKey() = runTest {
        coEvery { api.getItems("14.1.1", "en_US") } returns ItemResponse(mapOf("1036" to fakeItemFromApi))

        val result = repository.getRemoteItems("14.1.1", "en_US")

        assertEquals("1036", result.first().id)
    }

    @Test
    fun saveLocalItems_callsDao() = runTest {
        val item = fakeItemFromApi.copy(id = "1036")
        coJustRun { dao.clearAndInsertItems(listOf(item)) }

        repository.saveLocalItems(listOf(item))

        coVerify { dao.clearAndInsertItems(listOf(item)) }
    }

    @Test
    fun getLocalItems_returnsAllFromDao() = runTest {
        val item = fakeItemFromApi.copy(id = "1036")
        coEvery { dao.getAllItems() } returns listOf(item)

        assertEquals(listOf(item), repository.getLocalItems())
    }

}
