package com.zzy.champions.ui.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.items.compose.ItemRoute

const val ITEMS_ROUTE = "items"

fun NavGraphBuilder.itemsScreen() {
    composable(
        route = ITEMS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = tabEnterDirection(ITEMS_ROUTE, initialState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = tabExitDirection(ITEMS_ROUTE, targetState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
            )
        }
    ) { backStackEntry ->
        val refreshStamp by backStackEntry.savedStateHandle
            .getStateFlow(KEY_REFRESH, 0)
            .collectAsStateWithLifecycle()

        ItemRoute(
            refreshStamp = refreshStamp,
            onStampConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = 0 },
        )
    }
}
