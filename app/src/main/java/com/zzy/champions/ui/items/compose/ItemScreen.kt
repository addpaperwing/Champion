package com.zzy.champions.ui.items.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.zzy.champions.ui.compose.VersionText
import com.zzy.champions.ui.index.compose.SearchTextField
import com.zzy.champions.ui.items.CATEGORY_BOOTS
import com.zzy.champions.ui.items.CATEGORY_COMPONENTS
import com.zzy.champions.ui.items.CATEGORY_EPIC
import com.zzy.champions.ui.items.CATEGORY_LEGENDARY
import com.zzy.champions.ui.items.CATEGORY_MYTHIC
import com.zzy.champions.ui.items.CATEGORY_OTHER
import com.zzy.champions.ui.items.CATEGORY_STARTER
import com.zzy.champions.ui.items.ItemViewModel
import com.zzy.champions.ui.theme.Golden

private const val GRID_COLUMNS = 5
internal val itemCutCornerShape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
private val categoryHeaderBrush = Brush.horizontalGradient(listOf(Golden.copy(alpha = 0.25f), Color.Transparent))

private val categoryNameResIds = mapOf(
    CATEGORY_STARTER    to R.string.category_starter,
    CATEGORY_BOOTS      to R.string.category_boots,
    CATEGORY_MYTHIC     to R.string.category_mythic,
    CATEGORY_LEGENDARY  to R.string.category_legendary,
    CATEGORY_COMPONENTS to R.string.category_components,
    CATEGORY_EPIC       to R.string.category_epic,
    CATEGORY_OTHER      to R.string.category_other,
)

@Composable
fun ItemRoute(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel(),
    onSettingClick: () -> Unit = {},
    refreshStamp: Int = 0,
    onStampConsumed: () -> Unit = {},
) {
    LaunchedEffect(refreshStamp) {
        if (refreshStamp > 0) {
            viewModel.retry()
            onStampConsumed()
        }
    }

    val categorizedState by viewModel.categorizedItems.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val version by viewModel.version.collectAsStateWithLifecycle()
    val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler(enabled = searchText.isNotBlank()) {
        viewModel.updateSearchQuery("")
    }

    ItemScreen(
        modifier = modifier,
        categorizedState = categorizedState,
        version = version,
        searchText = searchText,
        onSearchTextChange = { viewModel.updateSearchQuery(it) },
        onSearchDone = { keyboardController?.hide() },
        onClearSearch = { viewModel.updateSearchQuery("") },
        onItemClick = viewModel::selectItem,
        onSettingClick = onSettingClick,
        onReloadClick = viewModel::retry,
    )

    val resolveItem = remember(viewModel) { viewModel::getItemById }
    val onComponentClick = remember(viewModel) { { componentId: String ->
        val resolved = resolveItem(componentId)
        if (resolved != null) viewModel.selectItem(resolved)
    } }
    // Don't overlay the error screen: dismiss the sheet when the load fails so
    // the user can reach the reload button.
    selectedItem?.takeIf { categorizedState !is UiState.Error }?.let { item ->
        ItemBottomSheet(
            item = item,
            version = version,
            onDismiss = viewModel::dismissItem,
            onComponentClick = onComponentClick,
            resolveItem = resolveItem,
        )
    }
}

@Composable
fun ItemScreen(
    modifier: Modifier = Modifier,
    categorizedState: UiState<List<Pair<String, List<Item>>>>,
    version: String,
    searchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    onSearchDone: () -> Unit = {},
    onClearSearch: (() -> Unit)? = null,
    onItemClick: (Item) -> Unit,
    onSettingClick: () -> Unit = {},
    onReloadClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        ItemsHeader(version = version, onSettingClick = onSettingClick)
        SearchTextField(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            text = searchText,
            onTextChanged = onSearchTextChange,
            onClearText = onClearSearch,
            onDone = { onSearchDone() },
        )
        when (categorizedState) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLUMNS),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    categorizedState.data.forEach { (categoryName, categoryItems) ->
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
            }
        }
    }
}

@Composable
private fun ItemsHeader(
    version: String,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VersionText(
            version = version,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        )
        IconButton(onClick = onSettingClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
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
