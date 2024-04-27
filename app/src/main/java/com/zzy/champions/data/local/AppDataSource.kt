package com.zzy.champions.data.local

import kotlinx.coroutines.flow.Flow

interface AppDataSource {
    fun getLanguage(): Flow<String>
    fun getVersion() : Flow<String>

    suspend fun setLanguage(language: String)
    suspend fun setVersion(version: String)
}