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
                navController.popBackStack()
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

internal const val NAV_ANIM_DURATION = 300
internal const val KEY_REFRESH = "refresh"

private fun NavHostController.signalRefresh() {
    for (tab in REFRESH_TABS) {
        val entry = runCatching { getBackStackEntry(tab.route) }.getOrNull() ?: continue
        entry.savedStateHandle.let { handle ->
            handle[KEY_REFRESH] = (handle.get<Int>(KEY_REFRESH) ?: 0) + 1
        }
    }
}
