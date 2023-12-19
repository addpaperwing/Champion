package com.zzy.champions.ui.detail.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.DetailViewModel

@Composable
fun ChampionDetailScreen(viewModel: DetailViewModel, id: String, onOpenBrowser: (String) -> Unit,) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    val builds by viewModel.builds.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = id) {
        viewModel.getChampionAndDetail(id)
        viewModel.getChampionBuilds()
    }

    if (result is UiState.Success) {
        val data = (result as UiState.Success).data
        Scaffold { padding ->
            ChampionDetail(
                modifier = Modifier.padding(padding),
                champion = data.champion,
                detail = data.detail,
                onSkinClick = {
                    viewModel.saveBannerSplash(data.detail, it)
                },
                championBuilds = builds,
                onBuildClick = {
                    onOpenBrowser(it)
                },
                onInsertBuild = { cb ->
                    viewModel.addChampionBuild(cb)
                },
                onEditBuild = { cb ->
                    viewModel.updateChampionBuild(cb)
                },
                onDeleteBuild = { cb ->
                    viewModel.deleteChampionBuild(cb)
                }
            )
        }
    }
}