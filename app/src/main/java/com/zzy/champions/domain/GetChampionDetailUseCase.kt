package com.zzy.champions.domain

import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class GetChampionDetailUseCase @Inject constructor(
    private val championRepository: ChampionRepository,
    private val appDataRepository: AppDataRepository,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(championId: String) : UiState<ChampionAndDetail> =  withContext(dispatcher) {
        if (championId.isBlank()) return@withContext UiState.Error(IOException("Champion not found"))

        var detail = championRepository.getLocalChampionDetail(championId)
        if (detail == null) {
            val version = appDataRepository.getLocalVersion().first() //Local version is always up to date in detail screen
            val language = appDataRepository.getLanguage().first()

            try {
                detail = championRepository.getRemoteChampionDetail(version, language, championId)?:throw IOException("Champion not found")
                championRepository.saveChampionDetail(detail)
            } catch (e: IOException) {
                return@withContext UiState.Error(e)
            }
        }
        return@withContext UiState.Success(championRepository.getChampionAndDetail(championId))
    }
}
