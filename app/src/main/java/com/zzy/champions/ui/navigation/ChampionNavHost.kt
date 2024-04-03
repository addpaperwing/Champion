package com.zzy.champions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.zzy.champions.ui.detail.compose.championDetailScreen
import com.zzy.champions.ui.index.compose.championIndexScreen


@Composable
fun ChampionNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    showLandingScreen: Boolean,
    onLandingScreenTimeout: () -> Unit,
    onLinkClick: (String) -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Index.route
    ) {
        championIndexScreen(
            showLandingScreen = showLandingScreen,
            onLandingScreenTimeout = onLandingScreenTimeout,
            onItemClick = {
                navController.navigateToChampionDetail(it.id)
            },
            onSettingClick = {
                navController.navigateSingleTopTo(Settings.route)
            }
        )

        championDetailScreen(onLinkClick)
//        composable(
//            route = Index.route,
//            enterTransition = {
//                fadeIn(animationSpec = tween(300, easing = LinearEasing)) + slideIntoContainer(
//                    animationSpec = tween(300, easing = EaseIn),
//                    towards = AnimatedContentTransitionScope.SlideDirection.Right
//                )
//            },
//            exitTransition = {
//                fadeOut(
//                    animationSpec = tween(
//                        300, easing = LinearEasing
//                    )
//                ) + slideOutOfContainer(
//                    animationSpec = tween(300, easing = EaseOut),
//                    towards = AnimatedContentTransitionScope.SlideDirection.Start
//                )
//            }
//        ) { entry ->
//            ChampionIndexScreen(viewModel = hiltViewModel(),
//                onSettingClick = {
//
//                }, onItemClick = {
//
//                },
//                versionAndLanguage = entry.savedStateHandle[ARG_KEY_VERSION_AND_LANGUAGE] ?:"")
//        }

//        composable(
//            route = Detail.routWithArgs,
//            arguments = Detail.arguments,
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
//        ) { entry ->
//            ChampionDetailScreen(entry.arguments?.getString(Detail.championIdArg)!!, onLinkClick)
//        }

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
//        launchSingleTop = true
        restoreState = true
    }

private fun NavHostController.navigateToChampionDetail(championId: String) {
    this.navigateSingleTopTo("${Detail.route}/$championId")
}