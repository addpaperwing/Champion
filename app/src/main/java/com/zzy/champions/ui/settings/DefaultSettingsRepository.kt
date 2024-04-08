package com.zzy.champions.ui.settings

import com.zzy.champions.data.local.LocalDataSource
import com.zzy.champions.data.remote.Api
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(
    private val api: Api,
    private val localDataSource: LocalDataSource
): SettingsRepository {

    override suspend fun getCurrentLanguage(): String  = localDataSource.getLanguage()

    override suspend fun getSupportLanguages(): List<String> = api.getSupportLanguage()

    override suspend fun getAppVersion(): String = localDataSource.getVersion()

    override suspend fun get6LatestVersion(): List<String> = api.getVersions().subList(0, 1)

    override suspend fun saveVersion(dataVersion: String) {
        localDataSource.setVersion(dataVersion)
    }

    override suspend fun saveLanguage(language: String) {
        localDataSource.setLanguage(language)
    }
}