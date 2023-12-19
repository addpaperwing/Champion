package com.zzy.champions.ui.index

import androidx.annotation.VisibleForTesting
import com.zzy.champions.data.local.ChampionBuildDao
import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.remote.Api
import java.io.IOException
import javax.inject.Inject

class DefaultChampionRepository @Inject constructor(
    private val api: Api,
    private val dsManager: DataStoreManager,
    private val cDao: ChampionDao,
    private val cbDao: ChampionBuildDao
): ChampionRepository {

    private var useRemote = false

    private var version: String? = null
    private var language: String? = null

    override suspend fun isFirstOpen(): Boolean = dsManager.isFirstOpen()

    override suspend fun setNotFirstOpen() {
        dsManager.setNotFirstOpen()
    }

    override suspend fun addChampionBuild(vararg builds: ChampionBuild) {
        cbDao.addNewBuild(*builds)
    }


    override suspend fun getLanguage(): String = language?:dsManager.getLanguage().also {
        language = it
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

    override suspend fun getPredictions(query: String): List<String> {
        return cDao.findChampion(query).map { it.name }
    }

    override suspend fun getChampions(name: String): List<Champion> {
        return cDao.findChampion(name)
    }

    @VisibleForTesting
    fun setUseRemote(remote: Boolean) {
        useRemote = remote
    }

    @VisibleForTesting
    fun getUseRemote() = useRemote
}