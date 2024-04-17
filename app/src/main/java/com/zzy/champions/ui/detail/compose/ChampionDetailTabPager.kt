package com.zzy.champions.ui.detail.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.ui.detail.compose.ability.Abilities
import com.zzy.champions.ui.detail.compose.build.ChampionBuildList
import com.zzy.champions.ui.detail.compose.skin.SkinList
import com.zzy.champions.ui.detail.compose.skin.rememberSkinListState
import com.zzy.champions.ui.detail.compose.stats.ChampionStats
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChampionDetailTabPager(
    modifier: Modifier = Modifier,
    onTabClick: (Int) -> Unit = {},
    champion: Champion,
    detail: ChampionDetail,
    initPage: Int = 0,
    onSkinClick:(SkinNumber) -> Unit = {},

    championBuilds: List<ChampionBuild> = emptyList(),
    onBuildClick: (String) -> Unit = {},
    onEditBuild: (ChampionBuild) -> Unit = {},
    onDeleteBuild: (Int) -> Unit = {}
    ) {
    val titles = stringArrayResource(id = R.array.tabs)
    val pagerState = rememberPagerState(initialPage = initPage) { titles.size }
    val scope = rememberCoroutineScope()

    Column(modifier) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp)
                        )
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            divider = {}
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.semantics { contentDescription = "$title tab" },
                    text = { Text(title, fontSize = 12.sp) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                        onTabClick(index)
                    },
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 4,
            verticalAlignment = Alignment.Top,
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> {
                    Abilities(abilities = detail.getAbilities())
                }
                1 -> {
                    ChampionStats(champion = champion)
                }
                2 -> {
                    ChampionBuildList(
                        modifier = Modifier.fillMaxHeight(),
                        builds = championBuilds,
                        onItemClick = { build ->
                            onBuildClick(build.getWebUrl(champion.name))
                        },
                        onEditBuild = onEditBuild,
                        onDeleteItem = onDeleteBuild
                    )
                }
                3 -> {
                    SkinList(
                        state = rememberSkinListState(detail.splashIndex),
                        championId = detail.championId,
                        skins = detail.skins,
                        onItemClick = onSkinClick
                    )
                }
            }
        }
    }
}