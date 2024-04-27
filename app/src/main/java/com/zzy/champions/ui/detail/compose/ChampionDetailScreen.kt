package com.zzy.champions.ui.detail.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.local.ChampionAndDetailPreviewParameterProvider
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.ChampionDetailViewModel
import com.zzy.champions.ui.detail.compose.header.GradientBannerImage
import com.zzy.champions.ui.detail.compose.header.InfoBox
import com.zzy.champions.ui.theme.MyApplicationTheme
import java.io.IOException

@Composable
fun ChampionDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: ChampionDetailViewModel = hiltViewModel(),
    onOpenBrowser: (String) -> Unit,
) {
    val version by viewModel.version.collectAsStateWithLifecycle()
    val championAndDetail by viewModel.result.collectAsStateWithLifecycle()
    val builds by viewModel.builds.collectAsStateWithLifecycle()

    ChampionDetailScreen(
        modifier = modifier,
        championDetailState = championAndDetail,
        version = version,
        onSaveBannerNumber = viewModel::saveBannerSplash,
        championBuilds = builds,
        addNewBuild = viewModel::addChampionBuild,
        onOpenBrowser = onOpenBrowser,
        updateBuild = viewModel::updateChampionBuild,
        deleteBuild = viewModel::deleteChampionBuild
    )
}



@Composable
fun ChampionDetailScreen(
    modifier: Modifier,
    championDetailState: UiState<ChampionAndDetail>,
    version: String,
    onSaveBannerNumber: (ChampionDetail, Int) -> Unit,

    championBuilds: List<ChampionBuild>,
    addNewBuild: (ChampionBuild) -> Unit,
    onOpenBrowser: (String) -> Unit,
    updateBuild: (ChampionBuild) -> Unit,
    deleteBuild: (Int) -> Unit,
) {
    var bannerUrl by rememberSaveable { mutableStateOf("") }
    var fabVisibility by rememberSaveable { mutableStateOf(false) }

    if (championDetailState is UiState.Success) {
        LaunchedEffect(championDetailState) {
            bannerUrl = championDetailState.data.detail.getSplash()
        }
        //For fab only
        Box {
            Column(
                modifier
                    .semantics { contentDescription = "Champion Detail" }
                    .fillMaxSize()

            ) {
                Box(Modifier.aspectRatio(1215 / 717f)) {
                    GradientBannerImage(Modifier.align(Alignment.TopCenter), bannerUrl)
                    InfoBox(
                        modifier = Modifier
                            .align(Alignment.BottomCenter),
                        title = championDetailState.data.champion.title,
                        name = championDetailState.data.champion.name,
                        tags = championDetailState.data.champion.tags,
                        lore = championDetailState.data.detail.lore
                    )
                }
                ChampionDetailTabPager(
                    onTabClick = { index ->
                        fabVisibility = (index == 2)
                    },
                    version = version,
                    champion = championDetailState.data.champion,
                    detail = championDetailState.data.detail,
                    onSkinClick = {
                        bannerUrl = championDetailState.data.detail.getSplash(it.num)
                        onSaveBannerNumber(championDetailState.data.detail, it.num)
                    },
                    championBuilds = championBuilds,
                    onBuildClick = onOpenBrowser,
                    onEditBuild = updateBuild,
                    onDeleteBuild = deleteBuild
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                visible = fabVisibility,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                AddChampionBuildFAB(onAddNewBuild = { name, url ->
                    addNewBuild(ChampionBuild(name, url))
                })
            }
        }
    } else {
        LoadingAndErrorScreen(
            isLoading = championDetailState is UiState.Loading,
            isError = championDetailState is UiState.Error
        ) {

        }
    }
}

@Preview
@Composable
fun PreviewDetailScreenChampionData(
    @PreviewParameter(ChampionAndDetailPreviewParameterProvider::class) championAndDetail: ChampionAndDetail
) {
    MyApplicationTheme {
            ChampionDetailScreen(
                modifier = Modifier,
                championDetailState = UiState.Success(championAndDetail),
                version = "",
                onSaveBannerNumber = {_,_ ->},

                championBuilds = listOf(
                    ChampionBuild("1","1"),
                    ChampionBuild("2","2"),
                    ChampionBuild("3","3")
                ),
                addNewBuild = {},
                onOpenBrowser = {},
                updateBuild = {},
                deleteBuild = {},
//                getChampionBuilds = {}
            )
    }
}

@Preview
@Composable
fun PreviewDetailScreenLoading() {
    MyApplicationTheme {
            ChampionDetailScreen(
                modifier = Modifier,
                championDetailState = UiState.Loading,
                version = "",
                onSaveBannerNumber = {_,_ ->},

                championBuilds = listOf(),
                addNewBuild = {},
                onOpenBrowser = {},
                updateBuild = {},
                deleteBuild = {},
//                getChampionBuilds = {}
            )
    }
}

@Preview
@Composable
fun PreviewDetailScreenError() {
    MyApplicationTheme {
            ChampionDetailScreen(
                modifier = Modifier,
                championDetailState = UiState.Error(IOException()),
                version = "",
                onSaveBannerNumber = {_,_ ->},

                championBuilds = listOf(),
                addNewBuild = {},
                onOpenBrowser = {},
                updateBuild = {},
                deleteBuild = {},
//                getChampionBuilds = {}
            )
    }
}