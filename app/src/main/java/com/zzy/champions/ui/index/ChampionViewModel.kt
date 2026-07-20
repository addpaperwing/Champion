package com.zzy.champions.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.domain.GetChampionDataUseCase
import com.zzy.champions.util.stateInViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
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
    private val appDataRepository: AppDataRepository,
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

    init {
        // Lives in this ViewModel's own scope rather than a nav-destination refresh signal:
        // the Champion tab's NavBackStackEntry is popped-with-saveState (not live) whenever
        // the user is on a different tab, so a signal routed through it is silently lost. This
        // ViewModel instance itself persists across tab switches (saveState/restoreState), so
        // collecting here reaches it regardless of which tab is currently visible.
        viewModelScope.launch {
            appDataRepository.dataRefreshed.collect { refresh() }
        }
    }

    fun updateSearchKeyword(query: String) { _query.value = query }

    fun refresh() { _refreshCounter.update { it + 1 } }  // atomic increment, never collides
}
