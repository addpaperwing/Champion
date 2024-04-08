package com.zzy.champions.domain

import com.zzy.champions.ui.index.ChampionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

class GetLanguageUseCase @Inject constructor(
    private val repository: ChampionRepository,
    private val dispatcher: CoroutineDispatcher,
) {

    private var cachedLanguage: String? = null

    suspend operator fun invoke() = flowOf(cachedLanguage ?: repository.getLanguage().also { cachedLanguage = it }).flowOn(dispatcher)

    @VisibleForTesting
    fun getCachedLanguage() = cachedLanguage
    @VisibleForTesting
    fun setCachedLanguage(language: String?) { cachedLanguage = language }
}