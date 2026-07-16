package com.zzy.champions.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.domain.GetChampionDataUseCase
import com.zzy.champions.util.stateInViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ChampionViewModel @Inject constructor(
    private val getChampionDataUseCase: GetChampionDataUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _refreshCounter = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val champions: StateFlow<UiState<ChampionData>> =
        merge(
            _query.debounce(300),                                     // keystrokes debounced
            _refreshCounter.filter { it > 0 }.map { _query.value }   // explicit refresh, no debounce
        )
            .map { getChampionDataUseCase(it) }
            .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    fun updateSearchKeyword(query: String) { _query.value = query }

    fun refresh() { _refreshCounter.update { it + 1 } }  // atomic increment, never collides
}
