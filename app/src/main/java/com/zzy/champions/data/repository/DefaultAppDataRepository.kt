package com.zzy.champions.data.repository

import com.zzy.champions.data.local.AppDataSource
import com.zzy.champions.data.remote.Api
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultAppDataRepository @Inject constructor(
    private val api: Api,
    private val appDataSource: AppDataSource,
) : AppDataRepository {

    override suspend fun getRemoteVersion(): List<String> = api.getVersions()

    override fun getLocalVersion(): Flow<String>  = appDataSource.getVersion()

    override suspend fun setLocalVersion(v: String) {
        appDataSource.setVersion(v)
    }

    override fun getLanguage(): Flow<String> = appDataSource.getLanguage()

    override suspend fun setLanguage(l: String) {
        appDataSource.setLanguage(l)
    }
}