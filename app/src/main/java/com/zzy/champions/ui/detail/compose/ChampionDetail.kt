package com.zzy.champions.ui.detail.compose

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.detail.compose.header.GradientBannerImage
import com.zzy.champions.ui.detail.compose.header.InfoBox
import com.zzy.champions.ui.theme.MyApplicationTheme
import java.math.BigDecimal

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChampionDetail(modifier: Modifier = Modifier,
                   champion: Champion,
                   detail: ChampionDetail,
                   onSkinClick:(Int) -> Unit = {},
                   championBuilds: List<ChampionBuild>,
                   onAddNewBuild: (String, String) -> Unit,
                   onBuildClick: (String) -> Unit,
                   onEditBuild: (ChampionBuild) -> Unit,
                   onDeleteBuild: (Int) -> Unit

) {
    var bannerImageUrl by remember { mutableStateOf("") }
    var fabVisibility by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = detail.championId) {
        bannerImageUrl = detail.getSplash()
    }

    //For fab only
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisibility,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                AddChampionBuildFAB(onAddNewBuild = { name, url ->
                    onAddNewBuild(name, url)
                })
            }
        }
    ) { _ ->
        Column(
            Modifier
                .fillMaxSize()

        ) {
            Box(Modifier.aspectRatio(1215 / 717f)) {
                GradientBannerImage(Modifier.align(Alignment.TopCenter), bannerImageUrl)
                InfoBox(
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    title = champion.title,
                    name = champion.name,
                    tags = champion.tags,
                    lore = detail.lore
                )
            }
            ChampionDetailTabPager(
                onTabClick = { index ->
                    fabVisibility = (index == 2)
                },
                champion = champion,
                detail = detail,
                onSkinClick = {
                    bannerImageUrl = detail.getSplash(it.num)
                    onSkinClick(it.num)
                },
                championBuilds = championBuilds,
                onBuildClick = onBuildClick,
                onEditBuild = onEditBuild,
                onDeleteBuild = onDeleteBuild
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xffffffff)
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
//        Scaffold { padding ->
            ChampionDetail(
                modifier = Modifier,
                champion = champion,
                detail = detail,
                onSkinClick = {

                },
                championBuilds = listOf(),
                onAddNewBuild = {_, _ ->

                },
                onBuildClick = {

                },
                onEditBuild = {

                },
                onDeleteBuild = {

                }
            )
//        }
    }
}
