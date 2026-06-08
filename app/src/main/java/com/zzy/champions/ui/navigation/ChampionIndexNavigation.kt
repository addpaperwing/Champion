package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.zzy.champions.data.model.Champion
import com.zzy.champions.ui.index.compose.ChampionIndexRoute

const val CHAMPION_INDEX_ROUTE = "index"
internal const val KEY_REFRESH = "refresh"

fun NavController.navigateToChampionIndex(navOptions: NavOptions) =
    navigate(CHAMPION_INDEX_ROUTE, navOptions)

fun NavGraphBuilder.championIndexScreen(
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit,
    onSplashFinished: () -> Unit = {},
) {
    composable(
        route = CHAMPION_INDEX_ROUTE,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Right
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        }
    ) { backStackEntry ->
        val shouldRefresh by backStackEntry.savedStateHandle
            .getStateFlow(KEY_REFRESH, false)
            .collectAsStateWithLifecycle()

        ChampionIndexRoute(
            onItemClick = onItemClick,
            onSettingClick = onSettingClick,
            shouldRefresh = shouldRefresh,
            onRefreshConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = false },
            onSplashFinished = onSplashFinished,
        )
    }
}
