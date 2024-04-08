package com.zzy.champions.ui.detail

import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail

interface DetailRepository {

//    suspend fun getChampion(id: String): Champion
    suspend fun getChampionAndDetail(id: String): ChampionAndDetail
    suspend fun updateChampionDetailSplash(detail: ChampionDetail)

    suspend fun getBuilds(): List<ChampionBuild>

    suspend fun addBuild(build: ChampionBuild): List<ChampionBuild>

    suspend fun editBuild(build: ChampionBuild): List<ChampionBuild>

    suspend fun deleteBuild(id: Int): List<ChampionBuild>
}