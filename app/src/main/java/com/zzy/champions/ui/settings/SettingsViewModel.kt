package com.zzy.champions.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.domain.GetChampionDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    val currentLanguage: StateFlow<String> = appDataRepository.getLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _languages = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val languages: StateFlow<UiState<List<String>>> = _languages.asStateFlow()

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        viewModelScope.launch(dispatcher) {
            _languages.value = try {
                UiState.Success(appDataRepository.getSupportedLanguages())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e)
            }
        }
    }

    fun selectLanguage(language: String, onDone: () -> Unit) {
        viewModelScope.launch(dispatcher) {
            appDataRepository.setLanguage(language)
            championRepository.clearLocalData()
            appDataRepository.setLocalVersion("0")
            getChampionDataUseCase.reset()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun refreshData(onDone: () -> Unit) {
        viewModelScope.launch(dispatcher) {
            championRepository.clearLocalData()
            appDataRepository.setLocalVersion("0")
            getChampionDataUseCase.reset()
            withContext(Dispatchers.Main) { onDone() }
        }
    }
}
