package com.zzy.champions.domain

import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import kotlinx.coroutines.CancellationException
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
                detail = (championRepository.getRemoteChampionDetail(version, language, championId)
                    ?: throw IOException("Champion not found"))
                    //Exclude skin variants whose names contain parentheses, except year variants like "Prestige K/DA Akali (2022)"
                    .let { it.copy(skins = it.skins.filterNot { skin -> skin.name.hasNonYearParentheses() }) }
                championRepository.saveChampionDetail(detail)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return@withContext UiState.Error(e)
            }
        }
        return@withContext UiState.Success(championRepository.getChampionAndDetail(championId))
    }
}

// Matches a parenthesized group that is NOT a bare 4-digit year, e.g. "(PROJECT)" but not "(2022)".
private val NON_YEAR_PARENTHESES = Regex("""\((?!\d{4}\))[^)]*\)""")

private fun String.hasNonYearParentheses(): Boolean = NON_YEAR_PARENTHESES.containsMatchIn(this)
