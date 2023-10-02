package com.zzy.champions.ui.index

import com.zzy.champions.data.model.Champion

interface ChampionRepository {
    suspend fun getLanguage(): String
    suspend fun getLatestVersion(): String
    suspend fun getAllChampions(version: String, language: String): List<Champion>

    suspend fun getPredictions(query: String): List<String>
    suspend fun getChampions(name: String): List<Champion>
}