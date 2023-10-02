package com.zzy.champions.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.ChampionAndDetail
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
class DetailViewModel @Inject constructor(
    private val repository: DetailRepository,
    private val dispatcher: CoroutineDispatcher
): ViewModel() {

    private val _result = MutableStateFlow<UiState<ChampionAndDetail>>(UiState.Loading)
    val result: StateFlow<UiState<ChampionAndDetail>> = _result.asStateFlow()

    fun getChampionAndDetail(id: String) {
        viewModelScope.launch {
            val result = withContext(dispatcher) {
                try {
                    val championAndDetail = repository.getChampionAndDetail(id)
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
                detail.skins.forEach {
                    it.initSelectState = skinNum == it.num
                }
                repository.updateChampionDetailSplash(detail)
            }
        }
    }
}