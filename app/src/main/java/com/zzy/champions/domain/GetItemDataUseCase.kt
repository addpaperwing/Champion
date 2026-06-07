package com.zzy.champions.domain

import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
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
    suspend operator fun invoke(): UiState<List<Item>> = withContext(dispatcher) {
        try {
            val cachedCount = itemRepository.getItemCount()
            if (cachedCount > 0) {
                return@withContext UiState.Success(itemRepository.getLocalItems())
            }
            val version = appDataRepository.getLocalVersion().first()
            val language = appDataRepository.getLanguage().first()
            val fetched = itemRepository.getRemoteItems(version, language)
                .filter { it.gold.purchasable }
            itemRepository.saveLocalItems(fetched)
            UiState.Success(itemRepository.getLocalItems())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            val cached = itemRepository.getLocalItems()
            if (cached.isNotEmpty()) UiState.Success(cached) else UiState.Error(e)
        }
    }
}
