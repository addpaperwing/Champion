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
import com.zzy.champions.ui.navigation.ChampionNavHost
import com.zzy.champions.ui.navigation.TOP_LEVEL_ROUTES
import com.zzy.champions.ui.navigation.TOP_LEVEL_TABS
import com.zzy.champions.ui.navigation.navigateSingleTopTo
import com.zzy.champions.ui.theme.DarkLight
import com.zzy.champions.ui.theme.Golden

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
                    TOP_LEVEL_TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateSingleTopTo(tab.route) },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = stringResource(tab.labelRes),
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = navItemColors,
                        )
                    }
                }
            }
        },
    ) { padding ->
        ChampionNavHost(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            navController = navController,
            onLinkClick = onLinkClick,
            onSplashFinished = appViewModel::onSplashFinished,
        )
    }
}
