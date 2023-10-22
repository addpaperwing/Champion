package com.zzy.champions.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private const val PREFERENCE_NAME_SETTING = "com.zzy.champions.settings"
private const val DEFAULT_LANGUAGE = "en_US"
private const val DEFAULT_VERSION = "13.19.1"

class DataStoreManager @Inject constructor(@ApplicationContext private val appContext: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME_SETTING)

    private val LANGUAGE = stringPreferencesKey("language")
    suspend fun getLanguage() = appContext.dataStore.data.first()[LANGUAGE]?: DEFAULT_LANGUAGE
    suspend fun setLanguage(language: String) = appContext.dataStore.edit { preferences ->
        preferences[LANGUAGE] = language
    }

    private val VERSION = stringPreferencesKey("version")
    suspend fun getVersion() = appContext.dataStore.data.first()[VERSION]?: DEFAULT_VERSION
    suspend fun setVersion(version: String) = appContext.dataStore.edit { preferences ->
        preferences[VERSION] = version
    }
}