package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.items.compose.ItemRoute

const val ITEMS_ROUTE = "items"

fun NavGraphBuilder.itemsScreen(onSettingClick: () -> Unit = {}) {
    composable(
        route = ITEMS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            val currIdx = TOP_LEVEL_TABS.indexOfFirst { it.route == ITEMS_ROUTE }
            val nextIdx = TOP_LEVEL_TABS.indexOfFirst { it.route == targetState.destination.route }
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
                towards = if (nextIdx in 0 until currIdx)
                    AnimatedContentTransitionScope.SlideDirection.End
                else
                    AnimatedContentTransitionScope.SlideDirection.Start
            )
        }
    ) { backStackEntry ->
        val refreshStamp by backStackEntry.savedStateHandle
            .getStateFlow(KEY_REFRESH, 0)
            .collectAsStateWithLifecycle()

        ItemRoute(
            onSettingClick = onSettingClick,
            refreshStamp = refreshStamp,
            onStampConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = 0 },
        )
    }
}
