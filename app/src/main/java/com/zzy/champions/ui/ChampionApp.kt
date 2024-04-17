package com.zzy.champions.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zzy.champions.ui.navigation.CHAMPION_DETAIL_ROUTE
import com.zzy.champions.ui.navigation.ChampionNavHost

@Composable
fun ChampionApp(
    modifier: Modifier,
    onLinkClick: (String) -> Unit
) {

    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination


    Scaffold(
        modifier = modifier,
        topBar = {
//            SettingAppbar(onBack = {})
        },
    ) { padding ->
        ChampionNavHost(
            modifier = modifier.padding(bottom = if (currentDestination?.route == CHAMPION_DETAIL_ROUTE) padding.calculateBottomPadding() else 0.dp),
            navController = navController,
            onLinkClick = onLinkClick
        )
    }
}