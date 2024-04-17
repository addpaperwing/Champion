package com.zzy.champions.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.navigation.CHAMPION_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class DetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DetailRepository,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _result = MutableStateFlow<UiState<ChampionAndDetail>>(UiState.Loading)
    val result: StateFlow<UiState<ChampionAndDetail>> = _result.asStateFlow()

    private val _builds = MutableStateFlow<List<ChampionBuild>>(emptyList())
    val builds: StateFlow<List<ChampionBuild>> = _builds.asStateFlow()

    fun getChampionAndDetail() {
        viewModelScope.launch {
            _result.value = UiState.Loading
            val result = withContext(dispatcher) {
                try {
                    val championAndDetail = repository.getChampionAndDetail(savedStateHandle.get<String>(CHAMPION_ID)?:"")
                    UiState.Success(championAndDetail)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    UiState.Error(e)
                }
            }
            _result.value = result
        }
    }

    fun saveBannerSplash(detail: ChampionDetail, skinNum: Int) {
        viewModelScope.launch {
            withContext(dispatcher) {
                detail.splashIndex = skinNum
                repository.updateChampionDetailSplash(detail)
            }
        }
    }

    fun getChampionBuilds() {
        viewModelScope.launch {
            _builds.value = withContext(dispatcher) {
                repository.getBuilds()
            }
        }
    }

    fun addChampionBuild(build: ChampionBuild) {
        viewModelScope.launch {
            _builds.value = withContext(dispatcher) {
                repository.addBuild(build)
            }
        }
    }

    fun updateChampionBuild(build: ChampionBuild) {
        viewModelScope.launch {
            _builds.value = withContext(dispatcher) {
                repository.editBuild(build)
            }
        }
    }

    fun deleteChampionBuild(id: Int) {
        viewModelScope.launch {
            _builds.value = withContext(dispatcher) {
                repository.deleteBuild(id)
            }
        }
    }
}