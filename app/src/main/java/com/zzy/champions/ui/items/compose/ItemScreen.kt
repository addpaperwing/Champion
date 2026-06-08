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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.R
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.compose.LoadingAndErrorScreen
import com.zzy.champions.ui.index.compose.SearchTextField
import com.zzy.champions.ui.items.ItemViewModel
import com.zzy.champions.ui.theme.Golden

private const val GRID_COLUMNS = 5

@Composable
fun ItemRoute(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel(),
    onSettingClick: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
) {
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.retry()
            onRefreshConsumed()
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

    selectedItem?.let { item ->
        ItemBottomSheet(
            item = item,
            version = version,
            onDismiss = viewModel::dismissItem,
            onComponentClick = { componentId ->
                viewModel.getItemById(componentId)?.let { viewModel.selectItem(it) }
            },
            resolveItem = viewModel::getItemById,
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
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            textAlign = TextAlign.End,
            text = if (version.isNotEmpty()) stringResource(R.string.v_, version) else "",
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = 8.sp,
        )
        IconButton(onClick = onSettingClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "settings",
                tint = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun CategoryHeader(name: String, modifier: Modifier = Modifier) {
    val shape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(top = 10.dp, bottom = 4.dp)
            .border(Dp.Hairline, Golden, shape)
            .background(
                Brush.horizontalGradient(listOf(Golden.copy(alpha = 0.25f), Color.Transparent)),
                shape,
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = name.uppercase(Locale.ROOT),
            color = Golden,
            fontWeight = FontWeight(700),
            fontSize = 11.sp,
            letterSpacing = 1.sp,
        )
    }
}
