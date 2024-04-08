package com.zzy.champions.domain

import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.index.ChampionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import org.jetbrains.annotations.VisibleForTesting
import java.io.IOException
import javax.inject.Inject

class GetAndSaveChampionBasicDataUseCase @Inject constructor(
    private val repository: ChampionRepository,
    private val dispatcher: CoroutineDispatcher,
    private val getLatestVersionUseCase: GetLatestVersionUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
) {

    private var localVersionIsNotLatest = true
    suspend operator fun invoke(query: String): Flow<UiState<ChampionData>> =
        getLatestVersionUseCase().combine(getLanguageUseCase()) { latestVersion, language ->
            val localVersion = repository.getLocalVersion()
            localVersionIsNotLatest = latestVersion != localVersion

            val dataVersion = if (localVersionIsNotLatest) {
                try {
                    val champions =
                        repository.getRemoteChampions(latestVersion, language).data.map { entry ->
                            entry.value
                        }
                    //Update local data
                    repository.saveLocalChampions(latestVersion, champions)
                    localVersionIsNotLatest = false

                    latestVersion
                } catch (e: IOException) {
                    //Skip updating local data and Use if error getting remote data
                    localVersion
                }
            } else {
                localVersion
            }
        UiState.Success(ChampionData(dataVersion, repository.searchChampionsBy(query)))
    }.flowOn(dispatcher)


    @VisibleForTesting
    fun localVersionIsNotLatest() = localVersionIsNotLatest
}