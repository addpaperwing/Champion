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
private const val KEY_SELECTED_CATEGORIES = "selected_categories"
private const val KEY_SELECTED_TAGS = "selected_tags"
private const val KEY_SELECTED_GAME_MODES = "selected_game_modes"

internal const val CATEGORY_STARTER    = "Starter"
internal const val CATEGORY_BOOTS      = "Boots"
internal const val CATEGORY_MYTHIC     = "Mythic"
internal const val CATEGORY_LEGENDARY  = "Legendary"
internal const val CATEGORY_COMPONENTS = "Components"
internal const val CATEGORY_EPIC       = "Epic"
internal const val CATEGORY_OTHER      = "Other"

internal val ALL_CATEGORIES = listOf(
    CATEGORY_STARTER,
    CATEGORY_BOOTS,
    CATEGORY_MYTHIC,
    CATEGORY_LEGENDARY,
    CATEGORY_COMPONENTS,
    CATEGORY_EPIC,
    CATEGORY_OTHER,
)

internal const val GAME_MODE_SUMMONERS_RIFT = "11"
internal const val GAME_MODE_ARAM           = "12"
internal const val GAME_MODE_ARENA          = "30"

internal val ALL_GAME_MODES = listOf(
    GAME_MODE_SUMMONERS_RIFT,
    GAME_MODE_ARAM,
    GAME_MODE_ARENA,
)

sealed interface ItemListDisplay {
    data class Categorized(val groups: List<Pair<String, List<Item>>>) : ItemListDisplay
    data class Flat(val items: List<Item>) : ItemListDisplay
}

private data class CategorizedData(
    val groups: List<Pair<String, List<Item>>>,
    val categoryByItemId: Map<String, String>,
)

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

    // Categorization (and the category-by-item-id index derived from it) is computed once
    // from raw items, not re-run on every search keystroke. Both are carried in one state
    // object so they can never drift out of sync with each other across combine emissions.
    private val _categorizedRawItems: StateFlow<UiState<CategorizedData>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    val groups = categorizeItems(state.data)
                    val categoryByItemId = groups.flatMap { (name, items) -> items.map { it.id to name } }.toMap()
                    UiState.Success(CategorizedData(groups, categoryByItemId))
                }
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    val selectedCategories: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_CATEGORIES, emptySet())
    val selectedTags: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_TAGS, emptySet())
    val selectedGameModes: StateFlow<Set<String>> = savedStateHandle.getStateFlow(KEY_SELECTED_GAME_MODES, emptySet())

    val availableTags: StateFlow<List<String>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.flatMap { it.tags }.filter { it !in ALL_CATEGORIES }.distinct().sorted()
                else -> emptyList()
            }
        }
        .stateInViewModel(viewModelScope, initialValue = emptyList())

    val itemListState: StateFlow<UiState<ItemListDisplay>> =
        combine(_categorizedRawItems, searchQuery, selectedCategories, selectedTags, selectedGameModes) { state, query, categories, tags, gameModes ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    val (groups, categoryByItemId) = state.data
                    if (categories.isEmpty() && tags.isEmpty() && gameModes.isEmpty()) {
                        val filtered = if (query.isBlank()) groups
                        else groups.mapNotNull { (name, items) ->
                            val matched = items.filter { it.name.contains(query, ignoreCase = true) }
                            if (matched.isEmpty()) null else name to matched
                        }
                        UiState.Success(ItemListDisplay.Categorized(filtered))
                    } else {
                        val flat = groups.flatMap { it.second }.filter { item ->
                            (categories.isEmpty() || categoryByItemId[item.id] in categories) &&
                                (tags.isEmpty() || tags.all { it in item.tags }) &&
                                (gameModes.isEmpty() || gameModes.all { item.maps[it] == true }) &&
                                (query.isBlank() || item.name.contains(query, ignoreCase = true))
                        }
                        UiState.Success(ItemListDisplay.Flat(flat))
                    }
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

    fun toggleCategoryFilter(category: String) {
        val current = selectedCategories.value
        savedStateHandle[KEY_SELECTED_CATEGORIES] = if (category in current) current - category else current + category
    }

    fun toggleTagFilter(tag: String) {
        val current = selectedTags.value
        savedStateHandle[KEY_SELECTED_TAGS] = if (tag in current) current - tag else current + tag
    }

    fun toggleGameMode(mapId: String) {
        val current = selectedGameModes.value
        savedStateHandle[KEY_SELECTED_GAME_MODES] = if (mapId in current) current - mapId else current + mapId
    }

    fun clearFilters() {
        savedStateHandle[KEY_SELECTED_CATEGORIES] = emptySet<String>()
        savedStateHandle[KEY_SELECTED_TAGS] = emptySet<String>()
        savedStateHandle[KEY_SELECTED_GAME_MODES] = emptySet<String>()
    }

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
