package com.zzy.champions.ui.index.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.compose.LaunchScreen
import com.zzy.champions.ui.index.ChampionViewModel
import com.zzy.champions.ui.navigation.Index

internal const val ARG_KEY_VERSION_AND_LANGUAGE = "version_and_language"

fun NavGraphBuilder.championIndexScreen(
    showLandingScreen: Boolean,
    onLandingScreenTimeout: () -> Unit,
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    composable(
        route = Index.route,
//        enterTransition = {
//            fadeIn(animationSpec = tween(300, easing = LinearEasing)) + slideIntoContainer(
//                animationSpec = tween(300, easing = EaseIn),
//                towards = AnimatedContentTransitionScope.SlideDirection.Right
//            )
//        },
//        exitTransition = {
//            fadeOut(
//                animationSpec = tween(
//                    300, easing = LinearEasing
//                )
//            )
////            + slideOutOfContainer(
////                animationSpec = tween(300, easing = EaseOut),
////                towards = AnimatedContentTransitionScope.SlideDirection.Start
////            )
//        }
    ) { entry ->
        ChampionIndexScreen(
            showLandingScreen = showLandingScreen,
            onLandingScreenTimeout = onLandingScreenTimeout,
            versionAndLanguage = entry.savedStateHandle[ARG_KEY_VERSION_AND_LANGUAGE] ?: "",
            onItemClick = onItemClick,
            onSettingClick = onSettingClick,
        )
    }
}

@Composable
fun ChampionIndexScreen(
    modifier: Modifier = Modifier,
    viewModel: ChampionViewModel = hiltViewModel(),
    showLandingScreen: Boolean,
    onLandingScreenTimeout: () -> Unit,
    versionAndLanguage: String = "",
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    val champions by viewModel.champions.collectAsStateWithLifecycle()
    var searchText by rememberSaveable { mutableStateOf("") }

    fun clearSearchTextAndReloadAllChampions() {
        searchText = ""
        viewModel.loadChampions()
    }

    LaunchedEffect(key1 = versionAndLanguage) {
        viewModel.insertBuildsWhenFirstOpen()
        viewModel.loadChampions()
    }

    BackHandler(enabled = searchText.isNotBlank()) {
        clearSearchTextAndReloadAllChampions()
    }

    if (!showLandingScreen && champions is UiState.Success) {
        ChampionIndex(
            modifier = modifier,
            searchText = searchText,
            onTextChanged = {
                searchText = it
                viewModel.getChampion(it)
            },
            onDoneActionClick = {
                viewModel.getChampion(it)
            },
            onClearSearchText = {
                clearSearchTextAndReloadAllChampions()
            },
            champions = (champions as UiState.Success).data,
            onSettingClick = onSettingClick,
            onItemClick = onItemClick
        )
    } else {
        LaunchScreen(modifier = Modifier, onTimeout = onLandingScreenTimeout)
    }
}