package com.zzy.champions.data.local.db

import com.zzy.champions.data.model.BUILD_OP_GG
import com.zzy.champions.data.model.BUILD_OP_GG_ARAM
import com.zzy.champions.data.model.BUILD_UGG
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import javax.inject.Inject

class ChampionDatabaseHelper @Inject constructor(
    private val cDao: ChampionDao,
    private val cdDao: ChampionDetailDao,
    private val cbDao: ChampionBuildDao
) {

    suspend fun updateChampionBasicData(data:  List<Champion>) {
        cDao.insertList(data)
        cdDao.clearDetailData()
    }

    suspend fun getAllChampionData(): List<Champion> {
        return cDao.getAll()
    }

    suspend fun searchChampions(query: String) : List<Champion> {
        return cDao.queryChampions(query)
    }


    suspend fun addPresetBuildData() {
        cbDao.addNewBuild(
            BUILD_OP_GG,
            BUILD_UGG,
            BUILD_OP_GG_ARAM,
        )
    }

    suspend fun getChampionDetail(id: String) = cdDao.getDetail(id)

    suspend fun updateChampionDetailData(championDetail: ChampionDetail) = cdDao.insert(championDetail)

    suspend fun getChampionBasicAndDetailData(id: String) : ChampionAndDetail  = cDao.getChampionAndDetail(id)



    suspend fun getChampionBuilds(): List<ChampionBuild> = cbDao.getBuilds()

    suspend fun addChampionBuild(build: ChampionBuild): List<ChampionBuild> = cbDao.addNewAndRefreshBuilds(build)

    suspend fun editChampionBuild(build: ChampionBuild): List<ChampionBuild> = cbDao.updateAndRefreshBuilds(build)

    suspend fun deleteChampionBuild(build: ChampionBuild): List<ChampionBuild> = cbDao.deleteAndRefreshBuilds(build)
}