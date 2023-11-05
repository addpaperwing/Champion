package com.zzy.champions.ui.settings

import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.remote.Api
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(
    private val api: Api,
    private val dsManager: DataStoreManager
): SettingsRepository {

    override suspend fun getCurrentLanguage(): String  = dsManager.getLanguage()

    override suspend fun getSupportLanguages(): List<String> = api.getSupportLanguage()

    override suspend fun getAppVersion(): String = dsManager.getVersion()

    override suspend fun get6LatestVersion(): List<String> = api.getVersions().subList(0, 1)

    override suspend fun saveVersion(dataVersion: String) {
        dsManager.setVersion(dataVersion)
    }

    override suspend fun saveLanguage(language: String) {
        dsManager.setLanguage(language)
    }
}