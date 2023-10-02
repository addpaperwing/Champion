package com.zzy.champions.ui.detail

import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetail

interface DetailRepository {
//    suspend fun getLanguage(): String
//    suspend fun getLatestVersion(): String

//    suspend fun getChampion(id: String): Champion
    suspend fun getChampionAndDetail(id: String): ChampionAndDetail
    suspend fun updateChampionDetailSplash(detail: ChampionDetail)
}