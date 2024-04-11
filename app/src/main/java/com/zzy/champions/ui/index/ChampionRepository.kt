package com.zzy.champions.ui.index

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionResponse

interface ChampionRepository {

    suspend fun isFirstOpen(): Boolean
//
    suspend fun setNotFirstOpen()

    suspend fun preloadDataForFirstOpen()
    suspend fun getRemoteVersion(): List<String>

    suspend fun getLocalVersion(): String

//    suspend fun setLocalVersion(version: String)

    suspend fun getLanguage(): String
    suspend fun getRemoteChampions(version: String, language: String): ChampionResponse
    suspend fun saveLocalChampions(version: String, championBasics: List<Champion>)
    suspend fun searchChampionsBy(id: String): List<Champion>

//    suspend fun getChampionAndDetail(id: String): ChampionAndDetail
//
//    suspend fun updateChampionDetailSplash(detail: ChampionDetail)
//
//    suspend fun getBuilds(): List<ChampionBuild>
//
//    suspend fun addBuild(build: ChampionBuild): List<ChampionBuild>
//
//    suspend fun editBuild(build: ChampionBuild): List<ChampionBuild>
//
//    suspend fun deleteBuild(build: ChampionBuild): List<ChampionBuild>
}