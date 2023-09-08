package com.zzy.champions.ui.grid

import com.zzy.champions.data.model.Champion

interface ChampionsRepository {
    suspend fun getLanguage(): String
    suspend fun getLatestVersion(): String
    suspend fun getChampions(version: String, language: String): List<Champion>
}