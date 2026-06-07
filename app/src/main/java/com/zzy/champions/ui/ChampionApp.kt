package com.zzy.champions.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zzy.champions.R
import com.zzy.champions.ui.navigation.CHAMPION_INDEX_ROUTE
import com.zzy.champions.ui.navigation.ChampionNavHost
import com.zzy.champions.ui.navigation.ITEMS_ROUTE
import com.zzy.champions.ui.navigation.navigateSingleTopTo

private val TOP_LEVEL_ROUTES = setOf(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE)

@Composable
fun ChampionApp(
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == CHAMPION_INDEX_ROUTE,
                        onClick = { navController.navigateSingleTopTo(CHAMPION_INDEX_ROUTE) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_champions),
                                contentDescription = "Champions",
                            )
                        },
                        label = { Text("Champions") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == ITEMS_ROUTE,
                        onClick = { navController.navigateSingleTopTo(ITEMS_ROUTE) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_items),
                                contentDescription = "Items",
                            )
                        },
                        label = { Text("Items") },
                    )
                }
            }
        },
    ) { padding ->
        ChampionNavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            onLinkClick = onLinkClick,
        )
    }
}
