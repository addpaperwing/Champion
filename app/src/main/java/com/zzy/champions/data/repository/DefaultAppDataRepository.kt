package com.zzy.champions.data.repository

import com.zzy.champions.data.local.AppDataSource
import com.zzy.champions.data.remote.Api
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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

    override suspend fun getSupportedLanguages(): List<String> = api.getSupportLanguage()

    private val _dataRefreshed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val dataRefreshed: SharedFlow<Unit> = _dataRefreshed.asSharedFlow()

    override fun notifyDataRefreshed() {
        _dataRefreshed.tryEmit(Unit)
    }
}