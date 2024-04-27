package com.zzy.champions.data.repository

import com.zzy.champions.data.model.ChampionBuild
import kotlinx.coroutines.flow.Flow

interface ChampionBuildRepository {

    fun getBuilds(): Flow<List<ChampionBuild>>

    suspend fun addBuild(build: ChampionBuild)

    suspend fun editBuild(build: ChampionBuild)

    suspend fun deleteBuild(id: Int)
}