package com.zzy.champions.ui.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.components.Banner
import com.zzy.champions.ui.components.GeneralInfo
import com.zzy.champions.ui.components.ChampionBuildScreen
import com.zzy.champions.ui.components.Abilities
import com.zzy.champions.ui.components.ChampionStats
import com.zzy.champions.ui.components.ChampionSkinsScreen
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabPagerSection(
    modifier: Modifier = Modifier,
    abilitiesContent: @Composable PagerScope.() -> Unit = {},
    skinsContent: @Composable PagerScope.() -> Unit = {},
    statsContent: @Composable PagerScope.() -> Unit = {},
    buildContent: @Composable PagerScope.() -> Unit = {}
) {
    val titles = stringArrayResource(id = R.array.tabs)
    val pagerState = rememberPagerState { titles.size }
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
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (page) {
                0 -> abilitiesContent()
                1 -> statsContent()
                2 -> buildContent()
                3 -> skinsContent()
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChampionDetail(modifier: Modifier = Modifier, champion: Champion, detail: ChampionDetail, onItemClick:(Int) -> Unit = {}) {
    var bannerImageUrl by remember { mutableStateOf("") }
    bannerImageUrl = detail.getSplash()
    Column(
        modifier
    ) {
        Box(Modifier.aspectRatio(1215 / 717f)) {
            Banner(Modifier.align(Alignment.TopCenter), bannerImageUrl)
            GeneralInfo(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                title = champion.title,
                name = champion.name,
                tags = champion.tags,
                lore = detail.lore
            )
        }
        TabPagerSection(
            abilitiesContent = {
                Abilities(
                    modifier = Modifier.fillMaxHeight(),
                    abilities = detail.getAbilities()
                )
            },
            skinsContent = {
                ChampionSkinsScreen(
                    championDetail = detail,
                    onItemClick = { skinNum ->
                        bannerImageUrl = detail.getSplash(skinNum.num)
                        onItemClick(skinNum.num)
                    }
                )
            },
            statsContent = {
                ChampionStats(champion = champion)
            },
            buildContent = {
                val list = listOf(
                    ChampionBuild(1, "OP.GG", "123123"),
                    ChampionBuild(2, "U.GG", "123123"),
                    ChampionBuild(3, "WP.GG", "123123")
                )
                ChampionBuildScreen(builds = list) {

                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewDetailScreen() {
    val champion = Champion(
        "aatrox",
        "star guardian seraphine The Darkin Blade",
        "The Darkin Blade",
        Image("Aatrox.png"),
        listOf("Warrior", "Fighter", "Assassin"),
        "Blood Well",
        Info(difficulty = 5),
        Stats(movespeed = BigDecimal(355), attackrange = BigDecimal(120))
    )
    val detail = ChampionDetail(
        "aatrox",
        listOf(
            SkinNumber(1, "Justicar Aatrox"),
            SkinNumber(2, "Mecha Aatrox"),
            SkinNumber(3, "Sea Hunter Aatrox"),
            SkinNumber(7, "Blood Moon Aatrox")
        ),
        "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. But after centuries of imprisonment, Aatrox was the first to find freedom once more, corrupting and transforming those foolish enough to try and wield the magical weapon that contained his essence. Now, with stolen flesh, he walks Runeterra in a brutal approximation of his previous form, seeking an apocalyptic and long overdue vengeance.",
        emptyList(),
        Passive(
            "p",
            "Periodically, Aatrox's next basic attack deals bonus <physicalDamage>physical damage</physicalDamage> and heals him, based on the target's max health.",
            Image("")
        )
    )
    MyApplicationTheme {
        Scaffold { padding ->
            ChampionDetail(
                modifier = Modifier.padding(padding),
                champion = champion,
                detail = detail
            )
        }
    }
}
