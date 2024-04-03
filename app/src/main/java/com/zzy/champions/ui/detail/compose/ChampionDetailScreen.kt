package com.zzy.champions.ui.detail.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.DetailViewModel
import com.zzy.champions.ui.navigation.Detail


fun NavGraphBuilder.championDetailScreen(
    onOpenBrowser: (String) -> Unit,
) {
    composable(
        route = Detail.routWithArgs,
        arguments = Detail.arguments,
//        enterTransition = {
//            fadeIn(animationSpec = tween(300, easing = LinearEasing))
////            + slideIntoContainer(
////                animationSpec = tween(300, easing = EaseIn),
////                towards = AnimatedContentTransitionScope.SlideDirection.Left
////            )
//        },
//        exitTransition = {
//            fadeOut(
//                animationSpec = tween(
//                    300, easing = LinearEasing
//                )
//            )
////            + slideOutOfContainer(
////                animationSpec = tween(300, easing = EaseOut),
////                towards = AnimatedContentTransitionScope.SlideDirection.End
////            )
//        }
    ) { entry ->
        ChampionDetailScreen(id = entry.arguments?.getString(Detail.championIdArg)!!, onOpenBrowser = onOpenBrowser)
    }
}

@Composable
fun ChampionDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
    id: String,
    onOpenBrowser: (String) -> Unit,
) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    val builds by viewModel.builds.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = id) {
        viewModel.getChampionAndDetail(id)
        viewModel.getChampionBuilds()
    }

    if (result is UiState.Success) {
        val data = (result as UiState.Success).data

        ChampionDetail(
            modifier = modifier,
            champion = data.champion,
            detail = data.detail,
            onSkinClick = {
                viewModel.saveBannerSplash(data.detail, it)
            },
            championBuilds = builds,
            onAddNewBuild = { build ->
                viewModel.addChampionBuild(build)
            },
            onBuildClick = {
                onOpenBrowser(it)
            },
            onEditBuild = { build ->
                viewModel.updateChampionBuild(build)
            },
            onDeleteBuild = { build ->
                viewModel.deleteChampionBuild(build)
            }
        )
    } else {
        //TODO error screen
    }
}