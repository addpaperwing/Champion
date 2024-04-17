package com.zzy.champions.ui.navigation

interface ChampionsDestination {
    val route: String
}

object Settings: ChampionsDestination {
    override val route: String = "setting"
}