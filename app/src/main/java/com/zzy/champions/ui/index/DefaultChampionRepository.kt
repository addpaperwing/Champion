package com.zzy.champions.ui.index

import androidx.annotation.VisibleForTesting
import com.zzy.champions.data.local.ChampionBuildDao
import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.ChampionDetailDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.BUILD_OP_GG
import com.zzy.champions.data.model.BUILD_OP_GG_ARAM
import com.zzy.champions.data.model.BUILD_UGG
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.Api
import java.io.IOException
import javax.inject.Inject

class DefaultChampionRepository @Inject constructor(
    private val api: Api,
    private val dsManager: DataStoreManager,
    private val cDao: ChampionDao,
    private val cdDao: ChampionDetailDao,
    private val cbDao: ChampionBuildDao
): ChampionRepository {

    private var useRemote = false

    private var version: String? = null
    private var language: String? = null

    override suspend fun preloadDataForFirstOpen() {
        if (dsManager.isFirstOpen()) {
            cbDao.addNewBuild(
                BUILD_OP_GG,
                BUILD_UGG,
                BUILD_OP_GG_ARAM,
            )
            dsManager.setNotFirstOpen()
        }
    }

    override suspend fun getVersion(): String {
        return version ?: run {
            val currentVersion = dsManager.getVersion()
            val latestVersion = try {
                api.getVersions()[0].also {
                    version = it
                }
            } catch (e: IOException) {
                currentVersion
            }

            Champion.version = latestVersion
            useRemote = latestVersion != currentVersion
            if (useRemote) dsManager.setVersion(latestVersion)
            latestVersion
        }
    }

    override suspend fun getLanguage(): String = language?:dsManager.getLanguage().also { language = it }

    override suspend fun getAllChampions(version: String, language: String): List<Champion> {
        return if (useRemote) {
            try {
                val result = api.getChampions(version, language).data.map { entry ->
                    entry.value
                }
                cDao.insertList(result)
                result
            } catch (e: IOException) {
                cDao.getAll()
            }
        } else {
            cDao.getAll()
        }
    }

    override suspend fun getChampions(name: String): List<Champion> {
        return cDao.findChampion(name)
    }

    override suspend fun getChampionAndDetail(id: String): ChampionAndDetail {
        //TODO Version first
        cdDao.getDetail(id)?: kotlin.run {
            val detail = api.getChampionDetail(dsManager.getVersion(), dsManager.getLanguage(), id).data[id]?: throw IOException("Champion not found")
            cdDao.insert(detail = detail)
        }
        return cDao.getChampionAndDetail(id)
    }

    override suspend fun updateChampionDetailSplash(detail: ChampionDetail) = cdDao.insert(detail)

    override suspend fun getBuilds(): List<ChampionBuild> = cbDao.getBuilds()

    override suspend fun addBuild(build: ChampionBuild): List<ChampionBuild> = cbDao.addNewAndRefreshBuilds(build)

    override suspend fun editBuild(build: ChampionBuild): List<ChampionBuild> = cbDao.updateAndRefreshBuilds(build)

    override suspend fun deleteBuild(build: ChampionBuild): List<ChampionBuild> = cbDao.deleteAndRefreshBuilds(build)

    @VisibleForTesting
    fun setUseRemote(remote: Boolean) {
        useRemote = remote
    }

    @VisibleForTesting
    fun getUseRemote() = useRemote
}