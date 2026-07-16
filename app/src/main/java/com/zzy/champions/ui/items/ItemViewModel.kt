package com.zzy.champions.ui.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.localVersionState
import com.zzy.champions.domain.GetItemDataUseCase
import com.zzy.champions.util.stateInViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_SEARCH_QUERY = "search_query"

internal const val CATEGORY_STARTER    = "Starter"
internal const val CATEGORY_BOOTS      = "Boots"
internal const val CATEGORY_MYTHIC     = "Mythic"
internal const val CATEGORY_LEGENDARY  = "Legendary"
internal const val CATEGORY_COMPONENTS = "Components"
internal const val CATEGORY_EPIC       = "Epic"
internal const val CATEGORY_OTHER      = "Other"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItemViewModel @Inject constructor(
    private val getItemDataUseCase: GetItemDataUseCase,
    private val appDataRepository: AppDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    private val _rawItems: StateFlow<UiState<List<Item>>> = _retryTrigger
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                emit(getItemDataUseCase())
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading, started = SharingStarted.Lazily)

    // Categorization is computed once from raw items, not re-run on every search keystroke.
    private val _categorizedRawItems: StateFlow<UiState<List<Pair<String, List<Item>>>>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> UiState.Success(categorizeItems(state.data))
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")

    val categorizedItems: StateFlow<UiState<List<Pair<String, List<Item>>>>> =
        combine(_categorizedRawItems, searchQuery) { state, query ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    if (query.isBlank()) state
                    else UiState.Success(state.data.mapNotNull { (name, items) ->
                        val filtered = items.filter { it.name.contains(query, ignoreCase = true) }
                        if (filtered.isEmpty()) null else name to filtered
                    })
                }
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    // Lazily (not WhileSubscribed) so the cached value persists after the UI goes to
    // background — prevents a blank version badge flash on return.
    val version: StateFlow<String> = appDataRepository.localVersionState(viewModelScope, started = SharingStarted.Lazily)

    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem.asStateFlow()

    // Survives retry(): keeps the last successful item list so BottomSheet lookups
    // continue to work while a new fetch is in-flight (when _rawItems = Loading).
    private var _lastKnownItems: Map<String, Item> = emptyMap()

    init {
        viewModelScope.launch {
            _rawItems.collect { state ->
                if (state is UiState.Success) _lastKnownItems = state.data.associateBy { it.id }
            }
        }
    }

    fun updateSearchQuery(query: String) { savedStateHandle[KEY_SEARCH_QUERY] = query }
    fun selectItem(item: Item) { _selectedItem.value = item }
    fun dismissItem() { _selectedItem.value = null }
    fun retry() { _retryTrigger.update { it + 1 } }

    fun getItemById(id: String): Item? = _lastKnownItems[id]
}

internal fun categorizeItems(items: List<Item>): List<Pair<String, List<Item>>> {
    val validItems = items.filter { it.id.isNotEmpty() }
    val allItemIds = validItems.map { it.id }.toSet()
    val componentIds = validItems.flatMap { it.components }.filter { it in allItemIds }.toSet()

    val categories = listOf(
        CATEGORY_STARTER    to { item: Item -> item.gold.total in 1..500 && item.id !in componentIds && "Boots" !in item.tags },
        CATEGORY_BOOTS      to { item: Item -> "Boots" in item.tags },
        CATEGORY_MYTHIC     to { item: Item -> "Mythic" in item.tags },
        CATEGORY_LEGENDARY  to { item: Item -> "Legendary" in item.tags },
        CATEGORY_COMPONENTS to { item: Item -> item.id in componentIds },
        CATEGORY_EPIC       to { item: Item -> item.gold.total >= 1000 },
        CATEGORY_OTHER      to { _: Item -> true },
    )

    val assigned = mutableSetOf<String>()
    return categories.mapNotNull { (name, predicate) ->
        val batch = validItems.filter { it.id !in assigned && predicate(it) }
        if (batch.isEmpty()) null
        else {
            assigned.addAll(batch.map { it.id })
            name to batch
        }
    }
}
