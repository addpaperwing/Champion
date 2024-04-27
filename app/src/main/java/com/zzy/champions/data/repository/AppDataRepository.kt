package com.zzy.champions.data.repository

import kotlinx.coroutines.flow.Flow

interface AppDataRepository {
    suspend fun getRemoteVersion(): List<String>

    fun getLocalVersion(): Flow<String>

    suspend fun setLocalVersion(v: String)

    fun getLanguage(): Flow<String>

    suspend fun setLanguage(l: String)
}