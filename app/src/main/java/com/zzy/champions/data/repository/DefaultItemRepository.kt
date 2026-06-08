package com.zzy.champions.data.repository

import com.zzy.champions.data.local.db.ItemDao
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.Api
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultItemRepository @Inject constructor(
    private val api: Api,
    private val dao: ItemDao,
    private val dispatcher: CoroutineDispatcher,
) : ItemRepository {

    override suspend fun getRemoteItems(version: String, language: String): List<Item> =
        withContext(dispatcher) {
            api.getItems(version, language).data.map { (id, item) -> item.copy(id = id) }
        }

    override suspend fun saveLocalItems(items: List<Item>) = withContext(dispatcher) {
        dao.clearAndInsertItems(items)
    }

    override suspend fun clearLocalItems() = withContext(dispatcher) {
        dao.clearItems()
    }

    override suspend fun getLocalItems(): List<Item> = withContext(dispatcher) {
        dao.getAllItems()
    }

    override suspend fun getItemCount(): Int = withContext(dispatcher) {
        dao.getItemCount()
    }

    override suspend fun getItemById(id: String): Item? = withContext(dispatcher) {
        dao.getItemById(id)
    }
}
