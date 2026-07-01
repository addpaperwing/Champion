package com.zzy.champions.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.domain.GetChampionDataUseCase
import com.zzy.champions.domain.GetItemDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val championRepository: ChampionRepository,
    private val getChampionDataUseCase: GetChampionDataUseCase,
    private val getItemDataUseCase: GetItemDataUseCase,
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

    fun selectLanguage(language: String, onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch(dispatcher) {
            clearAndRefresh(onDone, language = language)
        }
    }

    fun refreshData(onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch(dispatcher) {
            clearAndRefresh(onDone)
        }
    }

    // language is written after the cache clear so that a partial failure (e.g. Room throws)
    // leaves the language pref unchanged rather than committing a new locale over stale data.
    // reset() is always called (inner finally) so the in-memory version cache is invalidated even
    // if the repository calls throw, preventing a stale-cache-over-empty-DB state on the next fetch.
    // succeeded is set AFTER the inner try+finally so any exception from reset() (e.g. Room I/O
    // error in clearLocalItems) also yields onDone(false), preventing a split-brain where the
    // caller applies the new locale while the item cache was not actually cleared.
    // onDone(success) is in the outer finally so it fires regardless of whether the cleanup threw.
    private suspend fun clearAndRefresh(onDone: (success: Boolean) -> Unit, language: String? = null) {
        var succeeded = false
        try {
            try {
                championRepository.clearLocalData()
                appDataRepository.setLocalVersion("0")
                language?.let { appDataRepository.setLanguage(it) }
            } finally {
                getChampionDataUseCase.reset()
                getItemDataUseCase.reset()
            }
            succeeded = true
        } finally {
            withContext(NonCancellable + Dispatchers.Main) { onDone(succeeded) }
        }
    }
}
