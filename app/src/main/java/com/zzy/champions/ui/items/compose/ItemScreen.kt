package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.compose.LoadingAndErrorScreen
import com.zzy.champions.ui.items.ItemViewModel

private const val GRID_COLUMNS = 3

@Composable
fun ItemRoute(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel(),
) {
    val itemsState by viewModel.items.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val version = "14.24.1"  // TODO: wire from AppDataRepository in a follow-up

    ItemScreen(
        modifier = modifier,
        itemsState = itemsState,
        version = version,
        onItemClick = viewModel::selectItem,
    )

    selectedItem?.let { item ->
        ItemBottomSheet(
            item = item,
            version = version,
            onDismiss = viewModel::dismissItem,
            onComponentClick = { componentId ->
                if (itemsState is UiState.Success) {
                    val component = (itemsState as UiState.Success).data.find { it.id == componentId }
                    component?.let { viewModel.selectItem(it) }
                }
            },
        )
    }
}

@Composable
fun ItemScreen(
    modifier: Modifier = Modifier,
    itemsState: UiState<List<Item>>,
    version: String,
    onItemClick: (Item) -> Unit,
) {
    when (itemsState) {
        is UiState.Loading -> LoadingAndErrorScreen(
            isLoading = true,
            isError = false,
            onReloadClick = {},
            modifier = modifier,
        )
        is UiState.Error -> LoadingAndErrorScreen(
            isLoading = false,
            isError = true,
            onReloadClick = {},
            modifier = modifier,
        )
        is UiState.Success -> {
            val categories = categorizeItems(itemsState.data)
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                modifier = modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                categories.forEach { (categoryName, categoryItems) ->
                    item(span = { GridItemSpan(GRID_COLUMNS) }) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(categoryItems, key = { it.id }) { item ->
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

fun categorizeItems(items: List<Item>): List<Pair<String, List<Item>>> {
    val allItemIds = items.map { it.id }.toSet()
    val componentIds = items.flatMap { it.components }.filter { it in allItemIds }.toSet()

    val categories = listOf(
        "Starter"    to { item: Item -> item.gold.total <= 500 && item.id !in componentIds },
        "Boots"      to { item: Item -> "Boots" in item.tags },
        "Mythic"     to { item: Item -> "Mythic" in item.tags },
        "Legendary"  to { item: Item -> "Legendary" in item.tags },
        "Components" to { item: Item -> item.id in componentIds && "Mythic" !in item.tags && "Legendary" !in item.tags },
        "Epic"       to { item: Item -> item.gold.total in 1000..2999 },
        "Other"      to { _: Item -> true },
    )

    val assigned = mutableSetOf<String>()
    return categories.mapNotNull { (name, predicate) ->
        val batch = items.filter { it.id !in assigned && predicate(it) }
        if (batch.isEmpty()) null
        else {
            assigned.addAll(batch.map { it.id })
            name to batch
        }
    }
}
