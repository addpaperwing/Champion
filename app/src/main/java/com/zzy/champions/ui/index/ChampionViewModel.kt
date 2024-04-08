package com.zzy.champions.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.domain.GetAndSaveChampionBasicDataUseCase
import com.zzy.champions.domain.InsertBuildsForFirstOpenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChampionViewModel @Inject constructor(
    private val insertBuildsForFirstOpenUseCase: InsertBuildsForFirstOpenUseCase,
    private val getAndSaveChampionBasicDataUseCase: GetAndSaveChampionBasicDataUseCase,
): ViewModel() {

//    private val _champions = MutableStateFlow<UiState<List<Champion>>>(UiState.Loading)
//    val champions: StateFlow<UiState<List<Champion>>> = _champions.asStateFlow()

    private val _query: MutableStateFlow<String> = MutableStateFlow("")

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
//    private var getChampionJob: Job? = null


    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val champions: StateFlow<UiState<ChampionData>> =
        _query.asStateFlow().debounce(300).distinctUntilChanged().flatMapLatest {
            getAndSaveChampionBasicDataUseCase(it)
        }.onStart {
            emit(UiState.Loading)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )


    fun insertBuildsWhenFirstOpen() {
        viewModelScope.launch {
//            withContext(dispatcher) {
//                repository.preloadDataForFirstOpen()
//            }
            insertBuildsForFirstOpenUseCase()
        }

    }

//    fun loadChampions() {
//        viewModelScope.launch {
//            withContext(dispatcher) {
//                val champions = try {
//                    UiState.Success(repository.getAllChampions(repository.getVersion(), repository.getLanguage()))
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                    UiState.Error(e)
//                }
//
//                _champions.value = champions
//            }
//        }
//    }

    fun updateSearchKeyword(query: String) {
        _query.value = query
//        viewModelScope.launch {
//
//        }
    }
//    fun clearSearchResults() {
//        loadChampions()
//    }

//    fun getChampion(id: String) {
//        getChampionJob?.cancel()
//        getChampionJob = viewModelScope.launch {
//            val result = withContext(dispatcher) {
//                delay(300)
//                try {
//                    UiState.Success(repository.searchChampionsBy(id))
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                    UiState.Error(e)
//                }
//            }
//
//            _champions.value = result
//        }
//    }


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