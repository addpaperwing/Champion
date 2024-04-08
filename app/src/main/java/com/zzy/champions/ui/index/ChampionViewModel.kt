package com.zzy.champions.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

//    private val _query: MutableStateFlow<String> = MutableStateFlow("")

//    @OptIn(FlowPreview::class)
//    val predictions: Flow<List<Champion>> = _query.debounce(300)
//        .distinctUntilChanged()
//        .map {
//            if (it.isBlank()) return@map emptyList()
//            else repository.searchChampionsBy(it)
//        }.catch {
//            emit(emptyList())
//        }.flowOn(dispatcher)


//    private val _result = MutableStateFlow<UiState<ChampionAndDetail>>(UiState.Loading)
//    val result: StateFlow<UiState<ChampionAndDetail>> = _result.asStateFlow()
//
//    private val _builds = MutableStateFlow<List<ChampionBuild>>(emptyList())
//    val builds: StateFlow<List<ChampionBuild>> = _builds.asStateFlow()

    //Debounce
    private var getChampionJob: Job? = null


    fun insertBuildsWhenFirstOpen() {
        viewModelScope.launch {
            withContext(dispatcher) {
                repository.preloadDataForFirstOpen()
            }
        }
    }

    fun loadChampions() {
        viewModelScope.launch {
            withContext(dispatcher) {
                val champions = try {
                    UiState.Success(repository.getAllChampions(repository.getVersion(), repository.getLanguage()))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    UiState.Error(e)
                }

                _champions.value = champions
            }
        }
    }

//    fun updatePredictions(query: String) {
//        _query.value = query
//        viewModelScope.launch {
//
//        }
//    }
//    fun clearSearchResults() {
//        loadChampions()
//    }

    fun getChampion(id: String) {
        getChampionJob?.cancel()
        getChampionJob = viewModelScope.launch {
            val result = withContext(dispatcher) {
                delay(300)
                try {
                    UiState.Success(repository.searchChampionsBy(id))
                } catch (e: Throwable) {
                    e.printStackTrace()
                    UiState.Error(e)
                }
            }

            _champions.value = result
        }
    }


//    fun getChampionAndDetail(id: String) {
//        viewModelScope.launch {
//            val result = withContext(dispatcher) {
//                try {
//                    val championAndDetail = repository.getChampionAndDetail(id)
//                    UiState.Success(championAndDetail)
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                    UiState.Error(e)
//                }
//            }
//
//            _result.value = result
//        }
//    }
//
//    fun saveBannerSplash(detail: ChampionDetail, skinNum: Int) {
//        viewModelScope.launch {
//            withContext(dispatcher) {
//                detail.splashIndex = skinNum
//                detail.skins.forEach {
//                    it.initSelectState = skinNum == it.num
//                }
//                repository.updateChampionDetailSplash(detail)
//            }
//        }
//    }
//
//    fun getChampionBuilds() {
//        viewModelScope.launch {
//            _builds.value = withContext(dispatcher) {
//                repository.getBuilds()
//            }
//        }
//    }
//
//    fun addChampionBuild(build: ChampionBuild) {
//        viewModelScope.launch {
//            _builds.value = withContext(dispatcher) {
//                repository.addBuild(build)
//            }
//        }
//    }
//
//    fun updateChampionBuild(build: ChampionBuild) {
//        viewModelScope.launch {
//            _builds.value = withContext(dispatcher) {
//                repository.editBuild(build)
//            }
//        }
//    }
//
//    fun deleteChampionBuild(build: ChampionBuild) {
//        viewModelScope.launch {
//            _builds.value = withContext(dispatcher) {
//                repository.deleteBuild(build)
//            }
//        }
//    }
}