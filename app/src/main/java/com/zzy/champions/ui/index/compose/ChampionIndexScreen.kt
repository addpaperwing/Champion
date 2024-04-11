package com.zzy.champions.ui.index.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
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

fun NavGraphBuilder.championIndexScreen(
    showLandingScreen: Boolean,
    onLandingScreenTimeout: () -> Unit,
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    composable(
        route = Index.route,
        enterTransition = {
//            fadeIn(animationSpec = tween(300, easing = LinearEasing)) +
                    slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Right
            )
        },
        exitTransition = {
//            fadeOut(animationSpec = tween(300, easing = LinearEasing)) +
                    slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        }
    ) {
        ChampionIndexScreen(
            showLandingScreen = showLandingScreen,
            onLandingScreenTimeout = onLandingScreenTimeout,
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
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    val champions by viewModel.champions.collectAsStateWithLifecycle()
    var searchText by rememberSaveable { mutableStateOf("") }

    fun clearSearchTextAndReloadAllChampions() {
        searchText = ""
        viewModel.updateSearchKeyword(searchText)
    }

    LaunchedEffect(true) {
        viewModel.insertBuildsWhenFirstOpen()
        viewModel.updateSearchKeyword(searchText)
    }

    BackHandler(enabled = searchText.isNotBlank()) {
        clearSearchTextAndReloadAllChampions()
    }

    if (!showLandingScreen && champions is UiState.Success) {
        ChampionIndex(
            modifier = modifier,
            searchText = searchText,
            version = (champions as UiState.Success).data.version,
            onTextChanged = {
                searchText = it
                viewModel.updateSearchKeyword(it)
            },
            onDoneActionClick = {
                viewModel.updateSearchKeyword(it)
            },
            onClearSearchText = {
                clearSearchTextAndReloadAllChampions()
            },
            champions = (champions as UiState.Success).data.champions,
            onSettingClick = onSettingClick,
            onItemClick = onItemClick
        )
    } else {
        LaunchScreen(modifier = Modifier, onTimeout = onLandingScreenTimeout)
    }
}