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
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = CHAMPION_INDEX_ROUTE
    ) {
        championIndexScreen(
            onItemClick = { navController.navigateToChampionDetail(it.id) },
            onSettingClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } },
            onSplashFinished = onSplashFinished,
        )

        championDetailScreen(onLinkClick)

        settingsScreen(
            onBack = { navController.popBackStack() },
            onLanguageClick = { navController.navigate(LANGUAGE_ROUTE) { launchSingleTop = true } },
            onRefreshDone = {
                navController.signalRefresh()
                navController.popBackStack()
            }
        )

        languageScreen(
            onBack = { navController.popBackStack() },
            onLanguageSelected = {
                navController.signalRefresh()
                navController.popBackStack(CHAMPION_INDEX_ROUTE, inclusive = false)
            }
        )

        itemsScreen(
            onSettingClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } },
        )
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

// Add new top-level tabs here so they receive language-change and data-refresh signals.
private val REFRESHABLE_ROUTES = listOf(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE)

private fun NavHostController.signalRefresh() {
    for (route in REFRESHABLE_ROUTES) {
        try {
            getBackStackEntry(route).savedStateHandle[KEY_REFRESH] = true
        } catch (_: IllegalArgumentException) {
            // Entry not yet in back stack (screen never visited) — skip.
        }
    }
}
