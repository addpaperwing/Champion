package com.zzy.champions.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.SettingSelectable
import com.zzy.champions.data.remote.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _appVersion = MutableStateFlow("")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _availableVersions = MutableStateFlow<UiState<List<SettingSelectable>>>(UiState.Loading)
    val availableVersions: StateFlow<UiState<List<SettingSelectable>>> = _availableVersions.asStateFlow()

    private val _languages = MutableStateFlow<UiState<List<SettingSelectable>>>(UiState.Loading)
    val languages: StateFlow<UiState<List<SettingSelectable>>> = _languages.asStateFlow()

    private val _language = MutableStateFlow("")
    val language: StateFlow<String> = _language.asStateFlow()

    fun initData() {
        viewModelScope.launch {
            withContext(dispatcher) {
                initVersion()
                initLanguage()
            }
        }
    }

    private suspend fun initVersion() {
        val appVersion = repository.getAppVersion()

        try {
            val appVersions = ArrayList<SettingSelectable>()
            repository.get6LatestVersion().forEach {
                appVersions.add(SettingSelectable(it, it == appVersion))
            }
            _availableVersions.value = UiState.Success(appVersions)
        } catch (e: IOException) {
            e.printStackTrace()
            _availableVersions.value = UiState.Error(e)
        }

        _appVersion.value = appVersion
    }

    private suspend fun initLanguage() {
        val language = repository.getCurrentLanguage()

        try {
            val languages = repository.getSupportLanguages()
            val supportLanguages = ArrayList<SettingSelectable>()
            languages.forEach {
                supportLanguages.add(SettingSelectable(it, it == language))
            }
            _languages.value = UiState.Success(supportLanguages)
        } catch (e: IOException) {
            e.printStackTrace()
            _languages.value = UiState.Error(e)
        }

        _language.value = language
    }

    fun saveVersion(versionCode: String) {
        viewModelScope.launch {
            withContext(dispatcher) {
                repository.saveVersion(versionCode)
            }
        }
    }

    fun saveLanguage(language: String) {
        viewModelScope.launch {
            withContext(dispatcher) {
                repository.saveVersion(language)
            }
        }
    }
}