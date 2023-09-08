package com.zzy.champions.ui.grid

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.Api
import javax.inject.Inject

class DefaultChampionRepository @Inject constructor(
    private val api: Api,
    private val dsManager: DataStoreManager,
    private val dao: ChampionDao
): ChampionsRepository {

    private var useRemote = false

    override suspend fun getLanguage(): String = dsManager.getLanguage()

    override suspend fun getLatestVersion(): String {
        val latestVersion = api.getVersions()[0]
        val currentVersion = dsManager.getVersion()
        Champion.version = latestVersion
        useRemote =  latestVersion != currentVersion
        if (useRemote) dsManager.setVersion(latestVersion)
        return latestVersion
    }

    override suspend fun getChampions(version: String, language: String): List<Champion> {
        return if (useRemote) {
            val result = api.getChampions(version, language).data.map { entry ->
                entry.value
            }
            dao.insertList(result)
            result
        } else {
            dao.getAll()
        }
    }

    @VisibleForTesting
    fun setUseRemote(remote: Boolean) {
        useRemote = remote
    }

    @VisibleForTesting
    fun getUseRemote() = useRemote
}