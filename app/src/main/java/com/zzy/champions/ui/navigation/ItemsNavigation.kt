package com.zzy.champions.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.items.compose.ItemRoute

const val ITEMS_ROUTE = "items"

fun NavGraphBuilder.itemsScreen(onSettingClick: () -> Unit = {}) {
    composable(route = ITEMS_ROUTE) { backStackEntry ->
        val shouldRefresh by backStackEntry.savedStateHandle
            .getStateFlow(KEY_REFRESH, false)
            .collectAsStateWithLifecycle()

        ItemRoute(
            onSettingClick = onSettingClick,
            shouldRefresh = shouldRefresh,
            onRefreshConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = false },
        )
    }
}
