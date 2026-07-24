package com.zzy.champions.ui.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.data.model.Champion
import com.zzy.champions.ui.index.compose.ChampionIndexRoute

const val CHAMPION_INDEX_ROUTE = "index"

fun NavGraphBuilder.championIndexScreen(
    onItemClick: (Champion) -> Unit,
    onSplashFinished: () -> Unit = {},
) {
    composable(
        route = CHAMPION_INDEX_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = tabEnterDirection(CHAMPION_INDEX_ROUTE, initialState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseIn),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = tabExitDirection(CHAMPION_INDEX_ROUTE, targetState.destination.route),
                animationSpec = tween(NAV_ANIM_DURATION, easing = EaseOut),
            )
        }
    ) {
        ChampionIndexRoute(
            onItemClick = onItemClick,
            onSplashFinished = onSplashFinished,
        )
    }
}
