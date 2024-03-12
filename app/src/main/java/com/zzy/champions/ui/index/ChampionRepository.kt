package com.zzy.champions.ui.index

import com.zzy.champions.data.model.Champion

interface ChampionRepository {

//    suspend fun isFirstOpen(): Boolean
//
//    suspend fun setNotFirstOpen()
//    suspend fun addChampionBuild(vararg builds: ChampionBuild)

    suspend fun preloadDataForFirstOpen()
    suspend fun getVersion(): String

    suspend fun getLanguage(): String
    suspend fun getAllChampions(version: String, language: String): List<Champion>
    suspend fun searchChampionsBy(name: String): List<Champion>

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