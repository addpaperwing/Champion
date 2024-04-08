package com.zzy.champions.domain

import com.zzy.champions.ui.index.ChampionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertBuildsForFirstOpenUseCase @Inject constructor(
    private val repository: ChampionRepository,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke() = withContext(dispatcher) {
        if (repository.isFirstOpen()) {
            repository.preloadDataForFirstOpen()
            repository.setNotFirstOpen()
        }
    }
}