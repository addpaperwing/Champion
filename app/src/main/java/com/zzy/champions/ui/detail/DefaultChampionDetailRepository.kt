package com.zzy.champions.ui.detail

import androidx.annotation.VisibleForTesting
import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.Api
import java.io.IOException
import javax.inject.Inject

class DefaultChampionDetailRepository @Inject constructor(
    private val api: Api,
    private val dsManager: DataStoreManager,
    private val dao: ChampionDao
): ChampionDetailRepository {

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

    override suspend fun getChampion(id: String): Champion {
        return dao.findById(id)
    }

    override suspend fun getChampionDetail(version: String, language: String, id: String): ChampionDetail {
        return api.getChampionDetail(version, language, id).data[id]?: throw IOException("Champion not found")
    }

    @VisibleForTesting
    fun setUseRemote(remote: Boolean) {
        useRemote = remote
    }

    @VisibleForTesting
    fun getUseRemote() = useRemote
}