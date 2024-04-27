package com.zzy.champions.ui.index.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.local.ChampionDataPreviewParameterProvider
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.compose.LaunchScreen
import com.zzy.champions.ui.index.ChampionViewModel
import com.zzy.champions.ui.theme.MyApplicationTheme


@Composable
fun ChampionIndexRoute(
    modifier: Modifier = Modifier,
    viewModel: ChampionViewModel = hiltViewModel(),
    onSettingClick: () -> Unit,
    onItemClick: (Champion) -> Unit,
) {
    val champions by viewModel.champions.collectAsStateWithLifecycle()

    ChampionIndexScreen(
        modifier = modifier,
        championsState = champions,
        onUpdateSearchKeyword = viewModel::updateSearchKeyword,
        onInsertBuilds = {},
        onSettingClick = onSettingClick,
        onItemClick = onItemClick)
}

@Composable
fun ChampionIndexScreen(
    modifier: Modifier = Modifier,
    onboardingShowLandingScreen: Boolean = true,
    championsState: UiState<ChampionData>,
    onUpdateSearchKeyword: (String) -> Unit,
    onInsertBuilds: () -> Unit,
    onSettingClick: () -> Unit,
    onItemClick: (Champion) -> Unit,
) {
    var showLandingScreen by rememberSaveable { mutableStateOf(onboardingShowLandingScreen) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun clearSearchTextAndReloadAllChampions() {
        searchText = ""
        onUpdateSearchKeyword(searchText)
    }

    LaunchedEffect(Unit) {
        onInsertBuilds()
        onUpdateSearchKeyword(searchText)
    }

    BackHandler(enabled = searchText.isNotBlank()) {
        clearSearchTextAndReloadAllChampions()
    }

    if (!showLandingScreen && championsState is UiState.Success) {
        Column(modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Header(onSettingClick = onSettingClick, version = championsState.data.version)
            SearchTextField(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = searchText,
                onTextChanged = {
                    searchText = it
                    onUpdateSearchKeyword(searchText)
                },
                onClearText = {
                    clearSearchTextAndReloadAllChampions()
                },
                onDone = {
                    keyboardController?.hide()
                    onUpdateSearchKeyword(it)
                }
            )
            LazyVerticalGrid(
                modifier = Modifier.padding(top = 12.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(championsState.data.champions) {
                    ChampionCard(version = championsState.data.version, champion = it) {
                        onItemClick(it)
                    }
                }
            }
        }
    } else {
        LaunchScreen(modifier = Modifier, onTimeout = {
            showLandingScreen = false
        })
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewChampionIndexScreen(
    @PreviewParameter(ChampionDataPreviewParameterProvider::class)
    championData: ChampionData
) {
    MyApplicationTheme {
        Scaffold { padding ->
            ChampionIndexScreen(
                modifier = Modifier.padding(padding),
                onboardingShowLandingScreen = false,
                championsState = UiState.Success(championData),
                onUpdateSearchKeyword = {},
                onInsertBuilds = { },
                onSettingClick = { },
                onItemClick = { }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLandingScreen(
    @PreviewParameter(ChampionDataPreviewParameterProvider::class)
    championData: ChampionData
) {
    MyApplicationTheme {
        Scaffold { padding ->
            ChampionIndexScreen(
                modifier = Modifier.padding(padding),
                onboardingShowLandingScreen = true,
                championsState = UiState.Success(championData),
                onUpdateSearchKeyword = {},
                onInsertBuilds = { },
                onSettingClick = { },
                onItemClick = { }
            )
        }
    }
}