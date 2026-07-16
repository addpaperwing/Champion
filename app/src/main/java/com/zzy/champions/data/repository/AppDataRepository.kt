package com.zzy.champions.data.repository

import com.zzy.champions.data.local.PENDING_VERSION
import com.zzy.champions.util.DEFAULT_STATE_STOP_TIMEOUT_MS
import com.zzy.champions.util.stateInViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

interface AppDataRepository {
    suspend fun getRemoteVersion(): List<String>

    fun getLocalVersion(): Flow<String>

    suspend fun setLocalVersion(v: String)

    fun getLanguage(): Flow<String>

    suspend fun setLanguage(l: String)

    suspend fun getSupportedLanguages(): List<String>
}

// PENDING_VERSION (from DataStoreManager) leaks through getLocalVersion() unfiltered, so
// localVersionState() maps it to "" for every consumer instead of each call site re-deriving it.
fun AppDataRepository.localVersionState(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(DEFAULT_STATE_STOP_TIMEOUT_MS),
): StateFlow<String> = getLocalVersion()
    .map { if (it == PENDING_VERSION) "" else it }
    .stateInViewModel(scope, initialValue = "", started = started)

// Invalidates the cached local version so the next fetch is forced to treat it as stale,
// without callers needing to know the sentinel value getLocalVersion() will report meanwhile.
suspend fun AppDataRepository.invalidateLocalVersion() = setLocalVersion(PENDING_VERSION)