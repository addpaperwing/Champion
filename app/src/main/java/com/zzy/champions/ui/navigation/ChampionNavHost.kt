package com.zzy.champions.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zzy.champions.ui.detail.compose.ChampionDetailScreen
import com.zzy.champions.ui.index.compose.ChampionIndexScreen

internal val ARG_KEY_VERSION_AND_LANGUAGE = "version_and_language"

@Composable
fun ChampionNavHost(
    navController: NavHostController,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Index.route
    ) {

        composable(
            route = Index.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300, easing = LinearEasing)) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Right
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            }
        ) { entry ->
            ChampionIndexScreen(viewModel = hiltViewModel(),
                onSettingClick = {
                    navController.navigateSingleTopTo(Settings.route)
                }, onItemClick = {
                    navController.navigateToChampionDetail(it.id)
                },
                versionAndLanguage = entry.savedStateHandle[ARG_KEY_VERSION_AND_LANGUAGE] ?:"")
        }

        composable(
            route = Detail.routWithArgs,
            arguments = Detail.arguments,
            enterTransition = {
                fadeIn(animationSpec = tween(300, easing = LinearEasing)) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) { entry ->
            ChampionDetailScreen(hiltViewModel(), entry.arguments?.getString(Detail.championIdArg)!!, onLinkClick)
        }

//        composable(
//            route = Settings.route,
//            enterTransition = {
//                fadeIn(animationSpec = tween(300, easing = LinearEasing)) + slideIntoContainer(
//                    animationSpec = tween(300, easing = EaseIn),
//                    towards = AnimatedContentTransitionScope.SlideDirection.Left
//                )
//            },
//            exitTransition = {
//                fadeOut(
//                    animationSpec = tween(
//                        300, easing = LinearEasing
//                    )
//                ) + slideOutOfContainer(
//                    animationSpec = tween(300, easing = EaseOut),
//                    towards = AnimatedContentTransitionScope.SlideDirection.End
//                )
//            }
//        ) {
//            SettingsScreen(viewModel = hiltViewModel()) { version, language ->
//                navController.popBackStack()
//                navController.previousBackStackEntry?.savedStateHandle?.set(ARG_KEY_VERSION_AND_LANGUAGE, "$version$language")
//            }
//        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

private fun NavHostController.navigateToChampionDetail(championId: String) {
    this.navigateSingleTopTo("${Detail.route}/$championId")
}