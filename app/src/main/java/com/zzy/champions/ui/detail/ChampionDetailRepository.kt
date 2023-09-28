package com.zzy.champions.ui.detail

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail

interface ChampionDetailRepository {
    suspend fun getLanguage(): String
    suspend fun getLatestVersion(): String

    suspend fun getChampion(id: String): Champion
    suspend fun getChampionDetail(version: String, language: String, id: String): ChampionDetail
}