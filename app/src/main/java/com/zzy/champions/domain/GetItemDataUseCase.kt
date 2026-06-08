package com.zzy.champions.domain

import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetItemDataUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
    private val appDataRepository: AppDataRepository,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun reset() {
        itemRepository.clearLocalItems()
    }

    suspend operator fun invoke(): UiState<List<Item>> = withContext(dispatcher) {
        try {
            val cached = itemRepository.getLocalItems()
            if (cached.isNotEmpty()) {
                return@withContext UiState.Success(cached)
            }
            val (version, language) = coroutineScope {
                val v = async { appDataRepository.getLocalVersion().first() }
                val l = async { appDataRepository.getLanguage().first() }
                v.await() to l.await()
            }
            val fetched = itemRepository.getRemoteItems(version, language)
                .filter { it.gold.purchasable }
            if (appDataRepository.getLanguage().first() == language) {
                itemRepository.saveLocalItems(fetched)
                // If language changed mid-save, evict the stale data so the next
                // retry fetches with the correct language.
                if (appDataRepository.getLanguage().first() != language) {
                    itemRepository.clearLocalItems()
                }
            }
            UiState.Success(fetched)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            val cached = itemRepository.getLocalItems()
            if (cached.isNotEmpty()) UiState.Success(cached) else UiState.Error(e)
        }
    }
}
