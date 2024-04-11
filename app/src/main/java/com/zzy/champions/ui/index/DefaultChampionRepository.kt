package com.zzy.champions.ui.index

import com.zzy.champions.data.local.LocalDataSource
import com.zzy.champions.data.local.db.ChampionDatabaseHelper
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionResponse
import com.zzy.champions.data.remote.Api
import javax.inject.Inject

class DefaultChampionRepository @Inject constructor(
    private val api: Api,
    private val localDataSource: LocalDataSource,
    private val dbHelper: ChampionDatabaseHelper
): ChampionRepository {

//    private var useRemote = false
    override suspend fun isFirstOpen(): Boolean = localDataSource.isFirstOpen()

    override suspend fun setNotFirstOpen() = localDataSource.setNotFirstOpen()

    override suspend fun preloadDataForFirstOpen() {
        dbHelper.addPresetBuildData()
    }

    override suspend fun getRemoteVersion(): List<String> = api.getVersions()

    override suspend fun getLocalVersion(): String = localDataSource.getVersion()

    override suspend fun getLanguage(): String = localDataSource.getLanguage()

    override suspend fun getRemoteChampions(version: String, language: String): ChampionResponse = api.getChampions(version, language)

    override suspend fun saveLocalChampions(version: String, championBasics: List<Champion>) {
        localDataSource.setVersion(version)
        dbHelper.updateChampionBasicData(championBasics)
    }

    override suspend fun searchChampionsBy(id : String): List<Champion> {
        return dbHelper.searchChampionsById(id)
    }

//    @VisibleForTesting
//    fun setUseRemote(remote: Boolean) {
//        useRemote = remote
//    }
//
//    @VisibleForTesting
//    fun getUseRemote() = useRemote
}