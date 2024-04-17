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
import com.zzy.champions.data.model.BUILD_OP_GG
import com.zzy.champions.data.model.BUILD_OP_GG_ARAM
import com.zzy.champions.data.model.BUILD_UGG
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.DetailViewModel
import com.zzy.champions.ui.detail.compose.header.GradientBannerImage
import com.zzy.champions.ui.detail.compose.header.InfoBox
import com.zzy.champions.ui.detail.compose.header.rememberBannerState
import com.zzy.champions.ui.theme.MyApplicationTheme
import java.io.IOException

@Composable
fun ChampionDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
    onOpenBrowser: (String) -> Unit,
) {
    val championAndDetail by viewModel.result.collectAsStateWithLifecycle()
    val builds by viewModel.builds.collectAsStateWithLifecycle()

    ChampionDetailScreen(
        modifier = modifier,
        championDetailState = championAndDetail,
        getChampionDetail = viewModel::getChampionAndDetail,
        onSaveBannerNumber = viewModel::saveBannerSplash,
        championBuilds = builds,
        getChampionBuilds = viewModel::getChampionBuilds,
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
    getChampionDetail: () -> Unit,
    onSaveBannerNumber: (ChampionDetail, Int) -> Unit,
    initPage: Int = 0,

    championBuilds: List<ChampionBuild>,
    getChampionBuilds: () -> Unit,
    addNewBuild: (ChampionBuild) -> Unit,
    onOpenBrowser: (String) -> Unit,
    updateBuild: (ChampionBuild) -> Unit,
    deleteBuild: (Int) -> Unit,
) {
    val bannerState = rememberBannerState(initImageUrl = if (championDetailState is UiState.Success) championDetailState.data.detail.getSplash() else "")
    var fabVisibility by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(true) {
        getChampionDetail()
        getChampionBuilds()
    }

    if (championDetailState is UiState.Success) {
        //For fab only
        Box {
            Column(
                modifier
                    .semantics { contentDescription = "Champion Detail" }
                    .fillMaxSize()

            ) {
                Box(Modifier.aspectRatio(1215 / 717f)) {
                    GradientBannerImage(Modifier.align(Alignment.TopCenter), bannerState)
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
                    champion = championDetailState.data.champion,
                    detail = championDetailState.data.detail,
                    initPage = initPage,
                    onSkinClick = {
                        bannerState.imageUrl = championDetailState.data.detail.getSplash(it.num)
                        onSaveBannerNumber(championDetailState.data.detail, it.num)
                    },
                    championBuilds = championBuilds,
                    onBuildClick = onOpenBrowser,
                    onEditBuild = updateBuild,
                    onDeleteBuild = deleteBuild
                )
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
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
            getChampionDetail()
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
                getChampionDetail = {},
                onSaveBannerNumber = {_,_ ->},

                championBuilds = listOf(
                    BUILD_OP_GG,
                    BUILD_OP_GG_ARAM,
                    BUILD_UGG
                ),
                addNewBuild = {},
                onOpenBrowser = {},
                updateBuild = {},
                deleteBuild = {},
                getChampionBuilds = {}
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
                getChampionDetail = {},
                onSaveBannerNumber = {_,_ ->},

                championBuilds = listOf(),
                addNewBuild = {},
                onOpenBrowser = {},
                updateBuild = {},
                deleteBuild = {},
                getChampionBuilds = {}
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
                getChampionDetail = {},
                onSaveBannerNumber = {_,_ ->},

                championBuilds = listOf(),
                addNewBuild = {},
                onOpenBrowser = {},
                updateBuild = {},
                deleteBuild = {},
                getChampionBuilds = {}
            )
    }
}