package com.zzy.champions.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.components.LaunchScreen
import com.zzy.champions.ui.detail.ChampionDetail
import com.zzy.champions.ui.detail.DetailViewModel
import com.zzy.champions.ui.index.ChampionIndex
import com.zzy.champions.ui.index.ChampionViewModel
import com.zzy.champions.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                var showLandingScreen by rememberSaveable { mutableStateOf(true) }
                val navController = rememberNavController()

                if (showLandingScreen) {
                    LaunchScreen(modifier = Modifier, onTimeout = { showLandingScreen = false })
                } else {
                    Scaffold { padding ->
                        ChampionNavHost(navController = navController, modifier = Modifier.padding(padding))
                    }
                }
            }
        }
    }
}

@Composable
fun ChampionIndexScreen(viewModel: ChampionViewModel, onItemClick: (Champion) -> Unit) {
    val champions by viewModel.champions.collectAsStateWithLifecycle()
    val predictions by viewModel.predictions.collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(key1 = 1) {
        viewModel.getAllChampions()
    }

    BackHandler(enabled = predictions.isNotEmpty()) {
        viewModel.clearPredictions()
    }

    if (champions is UiState.Success) {
        ChampionIndex(modifier = Modifier.semantics { contentDescription = "Champion Index Screen" },
            predictions = predictions,
            onTextChanged = {
                viewModel.updatePredictions(it)
            },
            onDoneActionClick = {
                viewModel.clearPredictions()
                viewModel.getChampion(it)
            },
            onPredictionClick = {
                viewModel.clearPredictions()
                viewModel.getChampion(it)
            },
            champions = (champions as UiState.Success).data,
            onItemClick = onItemClick)
    }
}

@Composable
fun ChampionDetailScreen(viewModel: DetailViewModel, id: String) {
    val result by viewModel.result.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = id) {
        viewModel.getChampionAndDetail(id)
    }

    if (result is UiState.Success) {
        val data = (result as UiState.Success).data
        ChampionDetail(champion = data.champion, detail = data.detail) {
            viewModel.saveBannerSplash(data.detail, it)
        }
    }
}

@Composable
fun ChampionNavHost(
    navController: NavHostController,
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
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) {
            ChampionIndexScreen(viewModel = hiltViewModel(), onItemClick = {
                navController.navigateToChampionDetail(it.id)
            })
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
                    towards = AnimatedContentTransitionScope.SlideDirection.Right
                )
            }
        ) { entry ->
            ChampionDetailScreen(hiltViewModel(), entry.arguments?.getString(Detail.championIdArg)!!)
        }
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