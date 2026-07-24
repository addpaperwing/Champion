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
    onLinkClick: (String) -> Unit,
    onSplashFinished: () -> Unit = {},
) {
    val startRoute = TOP_LEVEL_TABS.first().route
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startRoute
    ) {
        championIndexScreen(
            onItemClick = { navController.navigateToChampionDetail(it.id) },
            onSplashFinished = onSplashFinished,
        )

        championDetailScreen(onLinkClick)

        settingsScreen(
            onLanguageClick = { navController.navigate(LANGUAGE_ROUTE) { launchSingleTop = true } },
        )

        languageScreen(
            onBack = { navController.popBackStack() },
            onLanguageSelected = { navController.popBackStack() },
        )

        itemsScreen()
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

internal const val NAV_ANIM_DURATION = 300
