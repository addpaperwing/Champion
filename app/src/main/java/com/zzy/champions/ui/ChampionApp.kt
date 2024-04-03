package com.zzy.champions.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zzy.champions.ui.navigation.ChampionNavHost
import com.zzy.champions.ui.navigation.Detail

@Composable
fun ChampionApp(
    modifier: Modifier,
    onLinkClick: (String) -> Unit
) {
    var showLandingScreen by rememberSaveable { mutableStateOf(true) }
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination


    Scaffold(
        modifier = modifier,
        topBar = {
//            SettingAppbar(onBack = {})
        },
    ) { padding ->
        ChampionNavHost(
            modifier = modifier.padding(bottom = if (currentDestination?.route == Detail.route) padding.calculateBottomPadding() else 0.dp),
            showLandingScreen = showLandingScreen,
            onLandingScreenTimeout = {
                showLandingScreen = false
            },
            navController = navController,
            onLinkClick = onLinkClick
        )
    }
}