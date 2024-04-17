package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zzy.champions.ui.detail.compose.ChampionDetailRoute


//object Detail: ChampionsDestination {
//    override val route: String = "detail"
//    const val championIdArg = "champion_id"
//
//    val routWithArgs = "$route/{$championIdArg}"
//    val arguments = listOf(
//        navArgument(championIdArg) { type = NavType.StringType }
//    )
//}

//private fun NavHostController.navigateToChampionDetail(championId: String) {
//    this.navigateSingleTopTo("${Detail.route}/$championId")
//}

const val CHAMPION_ID = "championId"
const val CHAMPION_DETAIL_ROUTE_BASE = "detail"
const val CHAMPION_DETAIL_ROUTE = "$CHAMPION_DETAIL_ROUTE_BASE/{$CHAMPION_ID}"

fun NavHostController.navigateToChampionDetail(championId: String) {
    this.navigateSingleTopTo("$CHAMPION_DETAIL_ROUTE_BASE/$championId")
}

fun NavGraphBuilder.championDetailScreen(
    onOpenBrowser: (String) -> Unit,
) {
    composable(
        route = CHAMPION_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(CHAMPION_ID) { type = NavType.StringType }
        ),
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }
    ) { _ ->
        ChampionDetailRoute(onOpenBrowser = onOpenBrowser)
    }
}