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
            // Read the language once after the fetch to detect mid-flight changes.
            val langAfterFetch = appDataRepository.getLanguage().first()
            return@withContext if (langAfterFetch == language) {
                itemRepository.saveLocalItems(fetched)
                // If language changed during the DB write, evict stale data so the
                // next retry re-fetches in the new language.
                if (appDataRepository.getLanguage().first() != language) {
                    itemRepository.clearLocalItems()
                    return@withContext UiState.Error(Exception("Language changed during save"))
                }
                UiState.Success(fetched)
            } else {
                // Language changed while the remote fetch was in flight; stale data not persisted.
                // LanguageScreen's onDone always calls onLanguageSelected(), which invokes signalRefresh()
                // in ChampionNavHost — so a retry in the correct language is always queued by the caller.
                UiState.Error(Exception("Language changed mid-fetch"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            val fallback = try { itemRepository.getLocalItems() } catch (ce: CancellationException) { throw ce } catch (_: Exception) { emptyList() }
            if (fallback.isNotEmpty()) UiState.Success(fallback) else UiState.Error(e)
        }
    }
}
