package com.zzy.champions.domain

import com.zzy.champions.data.local.PENDING_VERSION
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
                val v = async { resolveVersion() }
                val l = async { appDataRepository.getLanguage().first() }
                v.await() to l.await()
            }
            val fetched = itemRepository.getRemoteItems(version, language)
                .filter { it.gold.purchasable && it.inStore }
            // Read the language once after the fetch to detect mid-flight changes.
            val langAfterFetch = appDataRepository.getLanguage().first()
            return@withContext if (langAfterFetch == language) {
                itemRepository.saveLocalItems(fetched)
                appDataRepository.setLocalVersion(version)
                // If language changed during the DB write, evict stale data so the
                // next retry re-fetches in the new language.
                if (appDataRepository.getLanguage().first() != language) {
                    itemRepository.clearLocalItems()
                    return@withContext UiState.Error(Exception("Language changed during save"))
                }
                UiState.Success(fetched)
            } else {
                // Language changed while the remote fetch was in flight; stale data not persisted.
                // SettingsViewModel.clearAndRefresh() always calls appDataRepository.notifyDataRefreshed()
                // after a language switch, so a retry in the correct language is always queued by the caller.
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

    // The local version is only ever the PENDING_VERSION sentinel right after a manual
    // "refresh data" (SettingsViewModel.clearAndRefresh() invalidates it there, but NOT on a
    // plain language switch) or on a genuinely first-ever launch. In that case, resolve a real
    // version from the remote list — mirroring GetChampionDataUseCase's own cold-cache
    // resolution — instead of using the sentinel as a CDN path segment. Otherwise the existing
    // local version is already valid (a language switch doesn't change it), so reuse it
    // directly rather than paying for a remote round-trip on every item fetch.
    private suspend fun resolveVersion(): String {
        val local = appDataRepository.getLocalVersion().first()
        if (local != PENDING_VERSION) return local
        return try {
            appDataRepository.getRemoteVersion().firstOrNull() ?: local
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            local
        }
    }
}
