package com.zzy.champions.ui.settings

//class DefaultSettingsRepository @Inject constructor(
//    private val api: Api,
//    private val localDataSource: AppDataSource
//): SettingsRepository {
//
//    override suspend fun getCurrentLanguage(): String  = localDataSource.getLanguage()
//
//    override suspend fun getSupportLanguages(): List<String> = api.getSupportLanguage()
//
//    override suspend fun getAppVersion(): String = localDataSource.getVersion()
//
//    override suspend fun get6LatestVersion(): List<String> = api.getVersions().subList(0, 1)
//
//    override suspend fun saveVersion(dataVersion: String) {
//        localDataSource.setVersion(dataVersion)
//    }
//
//    override suspend fun saveLanguage(language: String) {
//        localDataSource.setLanguage(language)
//    }
//}