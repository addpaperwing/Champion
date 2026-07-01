package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.data.model.Champion
import com.zzy.champions.ui.index.compose.ChampionIndexRoute

const val CHAMPION_INDEX_ROUTE = "index"

fun NavGraphBuilder.championIndexScreen(
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit,
    onSplashFinished: () -> Unit = {},
) {
    composable(
        route = CHAMPION_INDEX_ROUTE,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        }
    ) { backStackEntry ->
        val refreshStamp by backStackEntry.savedStateHandle
            .getStateFlow(KEY_REFRESH, 0)
            .collectAsStateWithLifecycle()

        ChampionIndexRoute(
            onItemClick = onItemClick,
            onSettingClick = onSettingClick,
            refreshStamp = refreshStamp,
            onStampConsumed = { backStackEntry.savedStateHandle[KEY_REFRESH] = 0 },
            onSplashFinished = onSplashFinished,
        )
    }
}
