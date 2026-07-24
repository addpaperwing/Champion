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

data class ItemListDisplay(val groups: List<Pair<String, List<ItemGroup>>>)

// Data Dragon ships separately-tuned catalog entries for the same conceptual item across game
// modes (e.g. Arena's Infinity Edge is id "223031" at 2500g, Summoner's Rift's is "3031" at
// 3500g) — same name, different id/gold/stats/icon file (image.full always mirrors the item's
// own id, e.g. "223031.png" vs "3031.png", so it can never be used as a match key — verified
// against live Data Dragon data: every real mode-variant pair shares a name but never an icon
// filename). Grouping by name merges these into one card; the bottom sheet breaks variants back
// out by mode when they actually differ.
data class ItemGroup(val variants: List<Item>) {
    // Composite key: stable across recompositions since it's derived from sorted variant ids,
    // not affected by which variant happens to be picked as primary.
    val id: String = variants.map { it.id }.sorted().joinToString("+")

    // The shorter id is always Data Dragon's original/canonical entry — mode-specific reskins
    // are minted by prefixing digits onto the canonical id (e.g. Arena's "223031" from the
    // canonical "3031"), so they're always longer. Comparing (length, id) is fully deterministic
    // regardless of input order, with the id itself only breaking ties between equal-length ids.
    val primary: Item get() = variants.minWithOrNull(compareBy({ it.id.length }, { it.id })) ?: variants.first()
}

// Some same-named entries claim a curated mode a shorter-id sibling already claims instead of
// covering a genuinely new one — Ornn's "Masterwork" forge upgrade of a legendary item (e.g.
// Frozen Heart's id "323110" duplicating "3110"'s Summoner's Rift availability with boosted,
// non-purchasable stats), a champion-locked reprint (Kalista's Black Spear duplicated for
// Sylas's ultimate-steal), or a functionally-identical twin (Scorchclaw Pup's "1101"/"1107").
// None of these matter for planning a build — only the canonical (shortest-id) entry for each
// mode is kept; a variant covering a genuinely different mode (e.g. Mercury's Treads' separate
// ARAM/Arena entries) is unaffected and kept in full.
private fun dropRedundantModeVariants(variants: List<Item>): List<Item> {
    val ordered = variants.sortedWith(compareBy({ it.id.length }, { it.id }))
    val claimedModes = mutableSetOf<String>()
    return ordered.filter { variant ->
        val newModes = ALL_GAME_MODES.filter { variant.maps[it] == true && it !in claimedModes }
        if (newModes.isEmpty()) return@filter false
        claimedModes += newModes
        true
    }
}

internal fun groupItems(items: List<Item>): List<ItemGroup> =
    items.groupBy { it.name }.values.map { ItemGroup(dropRedundantModeVariants(it)) }

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
                emit(
                    when (val result = getItemDataUseCase()) {
                        is UiState.Success -> UiState.Success(result.data.filter { it.isAvailableOnACuratedGameMode() })
                        else -> result
                    },
                )
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading, started = SharingStarted.Lazily)

    private val _categorizedRawItems: StateFlow<UiState<List<Pair<String, List<ItemGroup>>>>> = _rawItems
        .map { state ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> UiState.Success(categorizeItems(groupItems(state.data)))
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
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
        combine(_categorizedRawItems, searchQuery, selectedTags, selectedGameModes) { state, query, tags, gameModes ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    val filtered = state.data.mapNotNull { (name, groups) ->
                        val matched = groups.filter { group ->
                            // Checked against every variant's tags (not just primary's) so a chip
                            // surfaced by availableTags — which also scans all variants — can
                            // always match the group that offered it.
                            (tags.isEmpty() || tags.all { tag -> group.variants.any { tag in it.tags } }) &&
                                (gameModes.isEmpty() || group.variants.any { v -> gameModes.all { v.maps[it] == true } }) &&
                                (query.isBlank() || group.primary.name.contains(query, ignoreCase = true))
                        }
                        if (matched.isEmpty()) null else name to matched
                    }
                    UiState.Success(ItemListDisplay(filtered))
                }
            }
        }
        .stateInViewModel(viewModelScope, initialValue = UiState.Loading)

    // Lazily (not WhileSubscribed) so the cached value persists after the UI goes to
    // background — prevents a blank version badge flash on return.
    val version: StateFlow<String> = appDataRepository.localVersionState(viewModelScope, started = SharingStarted.Lazily)

    private val _selectedItem = MutableStateFlow<ItemGroup?>(null)
    val selectedItem: StateFlow<ItemGroup?> = _selectedItem.asStateFlow()

    // Survives retry(): keeps the last successful item list so BottomSheet lookups continue to
    // work while a new fetch is in-flight (when _rawItems = Loading). Keyed by every variant's
    // raw id (not just each group's primary), since component/upgrade chips reference a specific
    // variant's id and must resolve to its owning group.
    private var _lastKnownGroups: Map<String, ItemGroup> = emptyMap()

    init {
        viewModelScope.launch {
            _rawItems.collect { state ->
                if (state is UiState.Success) {
                    _lastKnownGroups = groupItems(state.data)
                        .flatMap { group -> group.variants.map { it.id to group } }
                        .toMap()
                }
            }
        }
        // Lives in this ViewModel's own scope rather than a nav-destination refresh signal:
        // the Items tab's NavBackStackEntry is popped-with-saveState (not live) whenever the
        // user is on a different tab, so a signal routed through it is silently lost. This
        // ViewModel instance itself persists across tab switches (saveState/restoreState), so
        // collecting here reaches it regardless of which tab is currently visible.
        viewModelScope.launch {
            appDataRepository.dataRefreshed.collect { retry() }
        }
    }

    fun updateSearchQuery(query: String) { savedStateHandle[KEY_SEARCH_QUERY] = query }

    fun toggleTagFilter(tag: String) {
        val current = selectedTags.value
        savedStateHandle[KEY_SELECTED_TAGS] = if (tag in current) current - tag else current + tag
    }

    fun toggleGameMode(mapId: String) {
        val current = selectedGameModes.value
        savedStateHandle[KEY_SELECTED_GAME_MODES] = if (mapId in current) current - mapId else current + mapId
    }

    fun clearFilters() {
        savedStateHandle[KEY_SELECTED_TAGS] = emptySet<String>()
        savedStateHandle[KEY_SELECTED_GAME_MODES] = emptySet<String>()
    }

    fun selectItem(group: ItemGroup) { _selectedItem.value = group }
    fun dismissItem() { _selectedItem.value = null }
    fun retry() { _retryTrigger.update { it + 1 } }

    fun getGroupById(id: String): ItemGroup? = _lastKnownGroups[id]
}

// Only the three curated modes are ever offered as a filter (see ALL_GAME_MODES) — an item
// available only on some other map (Nexus Blitz, Odyssey, etc.) has nowhere to surface in this
// app, so it's excluded before grouping/categorization ever sees it, not just when its "any true
// value at all" check happened to pass.
internal fun Item.isAvailableOnACuratedGameMode() = ALL_GAME_MODES.any { maps[it] == true }

internal fun categorizeItems(groups: List<ItemGroup>): List<Pair<String, List<ItemGroup>>> {
    val validGroups = groups.filter { it.primary.id.isNotEmpty() }
    val allItemIds = validGroups.flatMap { g -> g.variants.map { it.id } }.toSet()
    val componentIds = validGroups.flatMap { g -> g.variants.flatMap { it.components } }.filter { it in allItemIds }.toSet()

    val categories = listOf(
        CATEGORY_STARTER    to { g: ItemGroup -> g.primary.gold.total in 1..500 && g.variants.none { it.id in componentIds } && "Boots" !in g.primary.tags },
        CATEGORY_BOOTS      to { g: ItemGroup -> "Boots" in g.primary.tags },
        CATEGORY_MYTHIC     to { g: ItemGroup -> "Mythic" in g.primary.tags },
        CATEGORY_LEGENDARY  to { g: ItemGroup -> "Legendary" in g.primary.tags },
        CATEGORY_COMPONENTS to { g: ItemGroup -> g.variants.any { it.id in componentIds } },
        CATEGORY_EPIC       to { g: ItemGroup -> g.primary.gold.total >= 1000 },
        CATEGORY_OTHER      to { _: ItemGroup -> true },
    )

    val assigned = mutableSetOf<String>()
    return categories.mapNotNull { (name, predicate) ->
        val batch = validGroups.filter { it.id !in assigned && predicate(it) }
        if (batch.isEmpty()) null
        else {
            assigned.addAll(batch.map { it.id })
            name to batch
        }
    }
}
