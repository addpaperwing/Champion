package com.zzy.champions.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
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
class ChampionDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ChampionDetailRepository,
    private val dispatcher: CoroutineDispatcher
): ViewModel() {

    private val _result = MutableStateFlow<UiState<Pair<Champion, ChampionDetail>>>(UiState.Loading)
    val result: StateFlow<UiState<Pair<Champion, ChampionDetail>>> = _result.asStateFlow()

    fun getChampionAndDetail(id: String) {
        viewModelScope.launch {
            val result = withContext(dispatcher) {
                try {
                    val champion = repository.getChampion(id)
                    val detail = repository.getChampionDetail(repository.getLatestVersion(), repository.getLanguage(), id)
                    UiState.Success(Pair(champion, detail))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    UiState.Error(e)
                }
            }

            _result.value = result
        }
    }
}