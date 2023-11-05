package com.zzy.champions.ui

import androidx.navigation.NavType
import androidx.navigation.navArgument

interface ChampionsDestination {
    val route: String
}

object Index: ChampionsDestination {
    override val route: String = "index"
}
object Detail: ChampionsDestination {
    override val route: String = "detail"
    const val championIdArg = "champion_id"

    val routWithArgs = "$route/{$championIdArg}"
    val arguments = listOf(
        navArgument(championIdArg) { type = NavType.StringType }
    )
}

object Settings: ChampionsDestination {
    override val route: String = "setting"
}