package com.zzy.champions.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ChampionIndexViewModel @Inject constructor(
    private val repository: ChampionIndexRepository,
    private val dispatcher: CoroutineDispatcher
): ViewModel() {

    private val _champions = MutableStateFlow<UiState<List<Champion>>>(UiState.Loading)
    val champions: StateFlow<UiState<List<Champion>>> = _champions.asStateFlow()

    fun getAllChampions() {
        viewModelScope.launch {
            val result = withContext(dispatcher) {
                try {
                    UiState.Success(repository.getChampions(repository.getLatestVersion(), repository.getLanguage()))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    UiState.Error(e)
                }
            }

            _champions.value = result
        }
    }
}