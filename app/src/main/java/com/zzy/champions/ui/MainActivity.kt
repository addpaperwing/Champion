package com.zzy.champions.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.zzy.champions.ui.detail.ChampionDetail
import com.zzy.champions.ui.detail.ChampionDetailViewModel
import com.zzy.champions.ui.index.ChampionIndexViewModel
import com.zzy.champions.ui.index.ChampionsGrid
import com.zzy.champions.ui.launch.LaunchScreen
import com.zzy.champions.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    private val viewModel: ChampionsViewModel by viewModels()

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
fun ChampionIndexScreen(viewModel: ChampionIndexViewModel, onItemClick: (Champion) -> Unit) {
    val champions by viewModel.champions.collectAsStateWithLifecycle()
    if (champions is UiState.Success) {
        ChampionsGrid(modifier = Modifier.semantics { contentDescription = "Champion Index Screen" }, champions = (champions as UiState.Success).data, onItemClick)
    }
}

@Composable
fun ChampionDetailScreen(viewModel: ChampionDetailViewModel) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    if (result is UiState.Success) {
        val data = (result as UiState.Success).data
        ChampionDetail(champion = data.first, detail = data.second)
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

        composable(route = Index.route) {
            val viewModel = hiltViewModel<ChampionIndexViewModel>()
            viewModel.getAllChampions()
            ChampionIndexScreen(viewModel = viewModel, onItemClick = {
                navController.navigate("${Detail.route}/${it.id}")
            })
        }

        composable(
            route = Detail.routWithArgs,
            arguments = Detail.arguments
        ) { entry ->
            //TODO handle null id
            val viewModel = hiltViewModel<ChampionDetailViewModel>()
            val championId = entry.arguments?.getString(Detail.championIdArg)!!
            viewModel.getChampionAndDetail(championId)
            ChampionDetailScreen(viewModel)
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