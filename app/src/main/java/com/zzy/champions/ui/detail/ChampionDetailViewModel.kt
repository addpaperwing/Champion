package com.zzy.champions.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionBuildRepository
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.data.repository.localVersionState
import com.zzy.champions.domain.GetChampionDetailUseCase
import com.zzy.champions.ui.navigation.CHAMPION_ID
import com.zzy.champions.util.stateInViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChampionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    appDataRepository: AppDataRepository,
    private val championRepository: ChampionRepository,
    private val championBuildRepository: ChampionBuildRepository,
    private val getChampionDetailUseCase: GetChampionDetailUseCase
) : ViewModel() {

    val version = appDataRepository.localVersionState(viewModelScope)

    val result: StateFlow<UiState<ChampionAndDetail>> =
        savedStateHandle.getStateFlow(key = CHAMPION_ID, initialValue = "")
            .map { id ->
                getChampionDetailUseCase(id)
            }.stateInViewModel(viewModelScope, initialValue = UiState.Loading)


    val builds: StateFlow<List<ChampionBuild>> = championBuildRepository.getBuilds()
        .stateInViewModel(viewModelScope, initialValue = emptyList())

    fun saveBannerSplash(detail: ChampionDetail, skinNum: Int) {
        viewModelScope.launch {
            championRepository.saveChampionDetail(detail.copy(splashIndex = skinNum))
        }
    }

    fun addChampionBuild(build: ChampionBuild) {
        viewModelScope.launch {
            championBuildRepository.addBuild(build)
        }
    }

    fun updateChampionBuild(build: ChampionBuild) {
        viewModelScope.launch {
            championBuildRepository.editBuild(build)
        }
    }

    fun deleteChampionBuild(id: Int) {
        viewModelScope.launch {
            championBuildRepository.deleteBuild(id)
        }
    }
}