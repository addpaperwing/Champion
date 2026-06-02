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
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = CHAMPION_INDEX_ROUTE
    ) {
        championIndexScreen(
            onItemClick = { navController.navigateToChampionDetail(it.id) },
            onSettingClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } },
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
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

// CHAMPION_INDEX_ROUTE is the startDestination and is always in the back stack when
// settings/language destinations are active, so getBackStackEntry won't throw here.
private fun NavHostController.signalRefresh() {
    try {
        getBackStackEntry(CHAMPION_INDEX_ROUTE).savedStateHandle[KEY_REFRESH] = true
    } catch (_: IllegalArgumentException) {
        // Index entry not in stack — navigation state is unexpected, skip the signal.
    }
}
