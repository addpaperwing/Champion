package com.zzy.champions.data.local

interface LocalDataSource {
    suspend fun getLanguage(): String
    suspend fun setLanguage(language: String)
    suspend fun getVersion() : String
    suspend fun setVersion(version: String)

    suspend fun isFirstOpen(): Boolean

    suspend fun setNotFirstOpen()
}