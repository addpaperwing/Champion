package com.zzy.champions.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ChampionViewModel @Inject constructor(
    private val repository: ChampionRepository,
    private val dispatcher: CoroutineDispatcher
): ViewModel() {

    private val _champions = MutableStateFlow<UiState<List<Champion>>>(UiState.Loading)
    val champions: StateFlow<UiState<List<Champion>>> = _champions.asStateFlow()

    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    @OptIn(FlowPreview::class)
    val predictions: Flow<List<String>> = _query.debounce(300)
        .distinctUntilChanged()
        .map {
            if (it.isBlank()) return@map emptyList()
            else repository.getPredictions(it)
        }.catch {
            emptyList<String>()
        }.flowOn(dispatcher)


    fun getAllChampions() {
        viewModelScope.launch {
            val result = withContext(dispatcher) {
                try {
                    UiState.Success(repository.getAllChampions(repository.getVersion(), repository.getLanguage()))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    UiState.Error(e)
                }
            }

            _champions.value = result
        }
    }

    fun updatePredictions(query: String) {
        _query.value = query
    }
    fun clearPredictions() {
        _query.value = ""
    }
    fun getChampion(name: String) {
        viewModelScope.launch {
            val result = withContext(dispatcher) {
                try {
                    UiState.Success(repository.getChampions(name))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    UiState.Error(e)
                }
            }

            _champions.value = result
        }
    }
}