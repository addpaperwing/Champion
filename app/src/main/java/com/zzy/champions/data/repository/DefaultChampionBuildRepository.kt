package com.zzy.champions.data.repository

import com.zzy.champions.data.local.db.ChampionBuildDao
import com.zzy.champions.data.model.ChampionBuild
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultChampionBuildRepository @Inject constructor(
    private val dao: ChampionBuildDao
): ChampionBuildRepository {

    override fun getBuilds(): Flow<List<ChampionBuild>> = dao.getBuilds()

    override suspend fun addBuild(build: ChampionBuild) {
        dao.addNewBuild(build)
    }

    override suspend fun editBuild(build: ChampionBuild) {
        dao.updateBuild(build)
    }

    override suspend fun deleteBuild(id: Int) {
        dao.deleteBuild(id)
    }
}