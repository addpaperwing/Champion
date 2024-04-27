package com.zzy.champions.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val PREFERENCE_NAME_SETTING = "com.zzy.champions.settings"
private const val DEFAULT_LANGUAGE = "en_US"
private const val DEFAULT_VERSION = "0"

class DataStoreManager @Inject constructor(@ApplicationContext private val appContext: Context): AppDataSource {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME_SETTING)

    private val LANGUAGE = stringPreferencesKey("language")
    private val VERSION = stringPreferencesKey("version")
    override fun getLanguage(): Flow<String> = appContext.dataStore.data.map { it[LANGUAGE]?: DEFAULT_LANGUAGE }.distinctUntilChanged()
    override fun getVersion() = appContext.dataStore.data.map { it[VERSION]?: DEFAULT_VERSION }.distinctUntilChanged()

    override suspend fun setLanguage(language: String) {
        appContext.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }

    override suspend fun setVersion(version: String) {
        appContext.dataStore.edit { preferences ->
            preferences[VERSION] = version
        }
    }
}