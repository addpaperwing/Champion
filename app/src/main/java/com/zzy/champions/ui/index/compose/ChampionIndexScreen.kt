package com.zzy.champions.ui.index.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlin.math.roundToInt


@Composable
fun ChampionIndexRoute(
    modifier: Modifier = Modifier,
    viewModel: ChampionViewModel = hiltViewModel(),
    onItemClick: (Champion) -> Unit,
    refreshStamp: Int = 0,
    onStampConsumed: () -> Unit = {},
    onSplashFinished: () -> Unit = {},
) {
    LaunchedEffect(refreshStamp) {
        if (refreshStamp > 0) {
            viewModel.refresh()
            onStampConsumed()
        }
    }

    val champions by viewModel.champions.collectAsStateWithLifecycle()

    ChampionIndexScreen(
        modifier = modifier,
        championsState = champions,
        onUpdateSearchKeyword = viewModel::updateSearchKeyword,
        onInsertBuilds = {},
        onItemClick = onItemClick,
        onSplashFinished = onSplashFinished,
    )
}

@Composable
fun ChampionIndexScreen(
    modifier: Modifier = Modifier,
    onboardingShowLandingScreen: Boolean = true,
    championsState: UiState<ChampionData>,
    onUpdateSearchKeyword: (String) -> Unit,
    onInsertBuilds: () -> Unit,
    onItemClick: (Champion) -> Unit,
    onSplashFinished: () -> Unit = {},
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
        // Keyed on locale + font scale (not the full Configuration.toString(), which embeds an
        // internal "seq" counter that changes on every recreation, including an ordinary
        // process restore) so only an actual locale/font-scale switch -- which can change the
        // header's natural height via different text wrap -- starts fresh, while an ordinary
        // tab switch or process restore keeps restoring the saved value.
        val configuration = LocalConfiguration.current
        val configKey = "${configuration.locales}-${configuration.fontScale}"
        // Natural (fully expanded) header height, captured once from the first
        // layout pass before any collapsing has been applied. rememberSaveable so the
        // collapse state matches the grid's own saved scroll position across tab switches.
        val headerHeightPx = rememberSaveable(key = "headerHeightPx:$configKey") { mutableFloatStateOf(0f) }
        // How many px of the header are currently collapsed away, in [0, headerHeightPx].
        val headerCollapsedPx = rememberSaveable(key = "headerCollapsedPx:$configKey") { mutableFloatStateOf(0f) }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                // Scrolling the grid up (available.y < 0) collapses the header first,
                // before the grid itself scrolls.
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y >= 0f || headerHeightPx.floatValue <= 0f) return Offset.Zero
                    val collapseBy = -available.y
                    val previous = headerCollapsedPx.floatValue
                    headerCollapsedPx.floatValue = (previous + collapseBy).coerceAtMost(headerHeightPx.floatValue)
                    return Offset(0f, -(headerCollapsedPx.floatValue - previous))
                }

                // Once the grid can't consume any more downward scroll (already at the
                // top), the leftover delta re-expands the header.
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (available.y <= 0f || headerHeightPx.floatValue <= 0f) return Offset.Zero
                    val expandBy = available.y
                    val previous = headerCollapsedPx.floatValue
                    headerCollapsedPx.floatValue = (previous - expandBy).coerceAtLeast(0f)
                    return Offset(0f, previous - headerCollapsedPx.floatValue)
                }
            }
        }

        Column(
            modifier = modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .nestedScroll(nestedScrollConnection)
        ) {
            // Reads headerHeightPx/headerCollapsedPx itself so only this small
            // composable recomposes on scroll, not the whole screen.
            CollapsingHeader(headerHeightPx = headerHeightPx, headerCollapsedPx = headerCollapsedPx)
            SearchTextField(
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
                modifier = Modifier.padding(top = 4.dp),
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
            onSplashFinished()
        })
    }
}

// Isolated into its own composable so a scroll-driven write to headerHeightPx/
// headerCollapsedPx only invalidates this small subtree, not the whole screen.
// Reads floatValue only inside layout()/graphicsLayer() lambdas (layout/draw phase),
// never in the composable body itself, so scrolling re-measures/re-draws this Box
// without recomposing Header's content on every scroll pixel.
@Composable
private fun CollapsingHeader(headerHeightPx: MutableFloatState, headerCollapsedPx: MutableFloatState) {
    Box(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val heightPx = headerHeightPx.floatValue
                val layoutHeight = if (heightPx > 0f) {
                    (heightPx - headerCollapsedPx.floatValue).coerceAtLeast(0f).roundToInt()
                } else {
                    placeable.height
                }
                layout(placeable.width, layoutHeight) {
                    placeable.placeRelative(0, 0)
                }
            }
            .clipToBounds()
    ) {
        Header(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    if (headerHeightPx.floatValue <= 0f) headerHeightPx.floatValue = coordinates.size.height.toFloat()
                }
                .graphicsLayer {
                    val heightPx = headerHeightPx.floatValue
                    alpha = if (heightPx > 0f) {
                        1f - (headerCollapsedPx.floatValue / heightPx).coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                }
        )
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
                onItemClick = { }
            )
        }
    }
}
