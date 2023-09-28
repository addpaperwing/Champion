package com.zzy.champions.ui.index

import com.zzy.champions.data.model.Champion

interface ChampionIndexRepository {
    suspend fun getLanguage(): String
    suspend fun getLatestVersion(): String
    suspend fun getChampions(version: String, language: String): List<Champion>
}