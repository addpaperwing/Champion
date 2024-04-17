package com.zzy.champions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost


@Composable
fun ChampionNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = CHAMPION_INDEX_ROUTE
    ) {
        championIndexScreen(
            onItemClick = {
                navController.navigateToChampionDetail(it.id)
            },
            onSettingClick = {
//                navController.navigateSingleTopTo(Settings.route)
            }
        )

        championDetailScreen(onLinkClick)
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
//        launchSingleTop = true
        restoreState = true
    }

