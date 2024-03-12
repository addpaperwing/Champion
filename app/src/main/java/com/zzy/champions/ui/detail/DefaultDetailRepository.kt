package com.zzy.champions.ui.detail

import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.local.db.ChampionDatabaseHelper
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.Api
import java.io.IOException
import javax.inject.Inject

class DefaultDetailRepository @Inject constructor(
    private val api: Api,
    private val dsManager: DataStoreManager,
    private val dbHelper: ChampionDatabaseHelper
): DetailRepository {

    override suspend fun getChampionAndDetail(id: String): ChampionAndDetail {
        dbHelper.getChampionDetail(id)?: kotlin.run {
            val detail = api.getChampionDetail(dsManager.getVersion(), dsManager.getLanguage(), id).data[id]?: throw IOException("Champion not found")
            dbHelper.updateChampionDetailData(detail)
        }
        return dbHelper.getChampionBasicAndDetailData(id)
    }

    override suspend fun updateChampionDetailSplash(detail: ChampionDetail) = dbHelper.updateChampionDetailData(detail)

    override suspend fun getBuilds(): List<ChampionBuild> = dbHelper.getChampionBuilds()

    override suspend fun addBuild(build: ChampionBuild): List<ChampionBuild> = dbHelper.addChampionBuild(build)

    override suspend fun editBuild(build: ChampionBuild): List<ChampionBuild> = dbHelper.editChampionBuild(build)

    override suspend fun deleteBuild(build: ChampionBuild): List<ChampionBuild> = dbHelper.deleteChampionBuild(build)
}