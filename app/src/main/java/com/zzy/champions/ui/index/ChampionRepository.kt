package com.zzy.champions.ui.index

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild

interface ChampionRepository {

    suspend fun isFirstOpen(): Boolean

    suspend fun setNotFirstOpen()
    suspend fun addChampionBuild(vararg builds: ChampionBuild)
    suspend fun getLanguage(): String
    suspend fun getVersion(): String
    suspend fun getAllChampions(version: String, language: String): List<Champion>

    suspend fun getPredictions(query: String): List<String>
    suspend fun getChampions(name: String): List<Champion>
}