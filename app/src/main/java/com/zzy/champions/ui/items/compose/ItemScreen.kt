package com.zzy.champions.ui.items.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.R
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.compose.LoadingAndErrorScreen
import com.zzy.champions.ui.index.compose.SearchTextField
import com.zzy.champions.ui.items.CATEGORY_BOOTS
import com.zzy.champions.ui.items.CATEGORY_COMPONENTS
import com.zzy.champions.ui.items.CATEGORY_EPIC
import com.zzy.champions.ui.items.CATEGORY_LEGENDARY
import com.zzy.champions.ui.items.CATEGORY_MYTHIC
import com.zzy.champions.ui.items.CATEGORY_OTHER
import com.zzy.champions.ui.items.CATEGORY_STARTER
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
import com.zzy.champions.ui.items.ItemListDisplay
import com.zzy.champions.ui.items.ItemViewModel
import com.zzy.champions.ui.theme.Golden

private const val GRID_COLUMNS = 5
internal val itemCutCornerShape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
private val categoryHeaderBrush = Brush.horizontalGradient(listOf(Golden.copy(alpha = 0.25f), Color.Transparent))

internal val categoryNameResIds = mapOf(
    CATEGORY_STARTER    to R.string.category_starter,
    CATEGORY_BOOTS      to R.string.category_boots,
    CATEGORY_MYTHIC     to R.string.category_mythic,
    CATEGORY_LEGENDARY  to R.string.category_legendary,
    CATEGORY_COMPONENTS to R.string.category_components,
    CATEGORY_EPIC       to R.string.category_epic,
    CATEGORY_OTHER      to R.string.category_other,
)

internal val gameModeNameResIds = mapOf(
    GAME_MODE_SUMMONERS_RIFT to R.string.game_mode_summoners_rift,
    GAME_MODE_ARAM           to R.string.game_mode_aram,
    GAME_MODE_ARENA          to R.string.game_mode_arena,
)

@Composable
fun ItemRoute(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel(),
    refreshStamp: Int = 0,
    onStampConsumed: () -> Unit = {},
) {
    LaunchedEffect(refreshStamp) {
        if (refreshStamp > 0) {
            viewModel.retry()
            onStampConsumed()
        }
    }

    val itemListState by viewModel.itemListState.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val version by viewModel.version.collectAsStateWithLifecycle()
    val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    val selectedGameModes by viewModel.selectedGameModes.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = searchText.isNotBlank()) {
        viewModel.updateSearchQuery("")
    }

    ItemScreen(
        modifier = modifier,
        itemListState = itemListState,
        version = version,
        searchText = searchText,
        onSearchTextChange = { viewModel.updateSearchQuery(it) },
        onSearchDone = { keyboardController?.hide() },
        onClearSearch = { viewModel.updateSearchQuery("") },
        isFilterActive = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || selectedGameModes.isNotEmpty(),
        onFilterIconClick = { showFilterSheet = true },
        selectedGameModes = selectedGameModes,
        onGameModeClear = viewModel::toggleGameMode,
        onItemClick = viewModel::selectItem,
        onReloadClick = viewModel::retry,
    )

    if (showFilterSheet) {
        ItemFilterBottomSheet(
            availableTags = availableTags,
            selectedCategories = selectedCategories,
            selectedTags = selectedTags,
            selectedGameModes = selectedGameModes,
            onCategoryToggle = viewModel::toggleCategoryFilter,
            onTagToggle = viewModel::toggleTagFilter,
            onGameModeToggle = viewModel::toggleGameMode,
            onClearAll = viewModel::clearFilters,
            onDismiss = { showFilterSheet = false },
        )
    }

    val resolveItem = remember(viewModel) { viewModel::getItemById }
    val onComponentClick = remember(viewModel) { { componentId: String ->
        val resolved = resolveItem(componentId)
        if (resolved != null) viewModel.selectItem(resolved)
    } }
    // Don't overlay the error screen: dismiss the sheet when the load fails so
    // the user can reach the reload button.
    selectedItem?.takeIf { itemListState !is UiState.Error }?.let { item ->
        ItemBottomSheet(
            item = item,
            version = version,
            onDismiss = viewModel::dismissItem,
            onComponentClick = onComponentClick,
            resolveItem = resolveItem,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemScreen(
    modifier: Modifier = Modifier,
    itemListState: UiState<ItemListDisplay>,
    version: String,
    searchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    onSearchDone: () -> Unit = {},
    onClearSearch: (() -> Unit)? = null,
    isFilterActive: Boolean = false,
    onFilterIconClick: () -> Unit = {},
    selectedGameModes: Set<String> = emptySet(),
    onGameModeClear: (String) -> Unit = {},
    onItemClick: (Item) -> Unit,
    onReloadClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        SearchTextField(
            text = searchText,
            onTextChanged = onSearchTextChange,
            onClearText = onClearSearch,
            onDone = { onSearchDone() },
            trailingContent = {
                FilterIconButton(isActive = isFilterActive, onClick = onFilterIconClick)
            },
        )
        if (selectedGameModes.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                selectedGameModes.forEach { mode ->
                    ActiveGameModeChip(
                        gameMode = mode,
                        onClear = { onGameModeClear(mode) },
                    )
                }
            }
        }
        when (itemListState) {
            is UiState.Loading -> LoadingAndErrorScreen(
                isLoading = true,
                isError = false,
                onReloadClick = onReloadClick,
            )
            is UiState.Error -> LoadingAndErrorScreen(
                isLoading = false,
                isError = true,
                onReloadClick = onReloadClick,
            )
            is UiState.Success -> {
                val display = itemListState.data
                if (display is ItemListDisplay.Flat && display.items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = stringResource(id = R.string.filter_no_results))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GRID_COLUMNS),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        when (display) {
                            is ItemListDisplay.Categorized -> {
                                display.groups.forEach { (categoryName, categoryItems) ->
                                    item(span = { GridItemSpan(GRID_COLUMNS) }, contentType = "header") {
                                        CategoryHeader(name = categoryName)
                                    }
                                    items(categoryItems, key = { it.id }, contentType = { "item" }) { item ->
                                        ItemCard(
                                            item = item,
                                            version = version,
                                            onClick = { onItemClick(item) },
                                        )
                                    }
                                }
                            }
                            is ItemListDisplay.Flat -> {
                                items(display.items, key = { it.id }, contentType = { "item" }) { item ->
                                    ItemCard(
                                        item = item,
                                        version = version,
                                        onClick = { onItemClick(item) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveGameModeChip(gameMode: String, onClear: () -> Unit, modifier: Modifier = Modifier) {
    val label = gameModeNameResIds[gameMode]?.let { stringResource(it) } ?: gameMode
    InputChip(
        selected = true,
        onClick = onClear,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.clear_game_mode_filter),
            )
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = Golden.copy(alpha = 0.25f),
            selectedLabelColor = Golden,
            selectedTrailingIconColor = Golden,
        ),
        modifier = modifier,
    )
}

@Composable
private fun CategoryHeader(name: String, modifier: Modifier = Modifier) {
    val localizedName = categoryNameResIds[name]?.let { stringResource(it) } ?: name
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(top = 10.dp, bottom = 4.dp)
            .border(Dp.Hairline, Golden, itemCutCornerShape)
            .background(categoryHeaderBrush, itemCutCornerShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = localizedName.uppercase(),
            color = Golden,
            fontWeight = FontWeight(700),
            fontSize = 11.sp,
            letterSpacing = 1.sp,
        )
    }
}
