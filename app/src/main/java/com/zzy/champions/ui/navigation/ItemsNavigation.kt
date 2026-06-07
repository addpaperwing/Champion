package com.zzy.champions.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.items.compose.ItemRoute

const val ITEMS_ROUTE = "items"

fun NavGraphBuilder.itemsScreen() {
    composable(route = ITEMS_ROUTE) {
        ItemRoute()
    }
}
