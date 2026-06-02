package com.zzy.champions.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.domain.GetChampionDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val championRepository: ChampionRepository,
    private val getChampionDataUseCase: GetChampionDataUseCase,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _languages = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val languages: StateFlow<UiState<List<String>>> = _languages.asStateFlow()

    init {
        viewModelScope.launch {
            _languages.value = try {
                val langs = withContext(dispatcher) { appDataRepository.getSupportedLanguages() }
                UiState.Success(langs)
            } catch (e: Exception) {
                UiState.Error(e)
            }
        }
    }

    fun selectLanguage(language: String, onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(dispatcher) {
                appDataRepository.setLanguage(language)
                appDataRepository.setLocalVersion("0")
                getChampionDataUseCase.reset()
            }
            onDone()
        }
    }

    fun refreshData(onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(dispatcher) {
                appDataRepository.setLocalVersion("0")
                getChampionDataUseCase.reset()
            }
            onDone()
        }
    }
}
