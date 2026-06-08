package com.zzy.champions.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zzy.champions.R
import com.zzy.champions.ui.navigation.CHAMPION_INDEX_ROUTE
import com.zzy.champions.ui.navigation.ChampionNavHost
import com.zzy.champions.ui.navigation.ITEMS_ROUTE
import com.zzy.champions.ui.navigation.navigateSingleTopTo
import com.zzy.champions.ui.theme.DarkLight
import com.zzy.champions.ui.theme.Golden

private val TOP_LEVEL_ROUTES = setOf(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE)

private val navItemColors @Composable get() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.Black,
    selectedTextColor = Golden,
    indicatorColor = Golden,
    unselectedIconColor = Color(0xff888888),
    unselectedTextColor = Color(0xff888888),
)

@Composable
fun ChampionApp(
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
) {
    val appViewModel: ChampionAppViewModel = hiltViewModel()
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (appViewModel.splashDone && currentRoute in TOP_LEVEL_ROUTES) {
                NavigationBar(containerColor = DarkLight) {
                    NavigationBarItem(
                        selected = currentRoute == CHAMPION_INDEX_ROUTE,
                        onClick = { navController.navigateSingleTopTo(CHAMPION_INDEX_ROUTE) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_champions),
                                contentDescription = stringResource(R.string.nav_champions),
                            )
                        },
                        label = { Text(stringResource(R.string.nav_champions)) },
                        colors = navItemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == ITEMS_ROUTE,
                        onClick = { navController.navigateSingleTopTo(ITEMS_ROUTE) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_items),
                                contentDescription = stringResource(R.string.nav_items),
                            )
                        },
                        label = { Text(stringResource(R.string.nav_items)) },
                        colors = navItemColors,
                    )
                }
            }
        },
    ) { padding ->
        ChampionNavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            onLinkClick = onLinkClick,
            onSplashFinished = appViewModel::onSplashFinished,
        )
    }
}
