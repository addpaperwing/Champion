package com.zzy.champions.domain

import com.zzy.champions.ui.index.ChampionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import org.jetbrains.annotations.VisibleForTesting
import java.io.IOException
import javax.inject.Inject

class GetLatestVersionUseCase @Inject constructor(
    private val repository: ChampionRepository,
    private val dispatcher: CoroutineDispatcher,
) {
    private var cachedVersion: String? = null

    suspend operator fun invoke() = flowOf(cachedVersion ?: try {
            repository.getRemoteVersion()[0].also {
                cachedVersion = it
            }
        } catch (e: IOException) {
            //Use local version if error getting remote version
            repository.getLocalVersion()
        })
        .flowOn(dispatcher)

    @VisibleForTesting
    fun getCachedVersion() = cachedVersion

    @VisibleForTesting
    fun setCachedVersion(ver: String) { cachedVersion = ver }
}