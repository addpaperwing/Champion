package com.zzy.champions.ui.index.compose

import androidx.activity.compose.BackHandler
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.compose.LaunchScreen
import com.zzy.champions.ui.index.ChampionViewModel

@Composable
fun ChampionIndexScreen(
    viewModel: ChampionViewModel,
    onSettingClick: () -> Unit,
    onItemClick: (Champion) -> Unit,
    versionAndLanguage: String = ""
) {
    val champions by viewModel.champions.collectAsStateWithLifecycle()
    val predictions by viewModel.predictions.collectAsStateWithLifecycle(emptyList())
    var showLandingScreen by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(key1 = versionAndLanguage) {
        viewModel.insertBuildsWhenFirstOpen()
        viewModel.loadChampions()
    }

    BackHandler(enabled = predictions.isNotEmpty()) {
        viewModel.clearPredictions()
    }

    if (!showLandingScreen && champions is UiState.Success) {
        Scaffold { padding ->
            ChampionIndex(
                modifier = Modifier
                    .padding(padding)
                    .semantics { contentDescription = "Champion Index Screen" },
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
                onSettingClick = onSettingClick,
                onItemClick = onItemClick
            )
        }
    } else {
        LaunchScreen(modifier = Modifier, onTimeout = { showLandingScreen = false })
    }
}