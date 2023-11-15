package com.zzy.champions.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.ui.theme.Golden
import com.zzy.champions.ui.theme.MyApplicationTheme


//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun Banner(modifier: Modifier = Modifier, pagerState: PagerState, detail: ChampionDetail) {
//    Box(modifier = modifier) {
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1215 / 717f),
//            userScrollEnabled = false
//        ) { page ->
//            Column(Modifier.fillMaxHeight()) {
//                AsyncImage(
//                    model = detail.getSplash(),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .aspectRatio(1215 / 717f)
//                )
//            }
//        }
//        Box(
//            Modifier
//                .align(Alignment.BottomStart)
//                .fillMaxWidth()
//                .aspectRatio(1215 / 717f)
//                .background(
//                    Brush.verticalGradient(
//                        listOf(
//                            Color.Transparent, MaterialTheme.colorScheme.primary
//                        )
//                    )
//                )
//        )
//    }
//}

//@Composable
//fun SwitchSkinButton(
//    modifier: Modifier = Modifier,
//    detail: ChampionDetail,
//    pageSize: Int,
//    onPrevClick: () -> Unit,
//    onNextClick: () -> Unit
//) {
//    var skinNameCount by remember { mutableIntStateOf(0) }
//    var forward by remember { mutableStateOf(true) }
//    Surface(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(32.dp),
//        shape = CutCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
//        color = MaterialTheme.colorScheme.onTertiary,
//        tonalElevation = 6.dp,
//        shadowElevation = 6.dp,
//    ) {
//        Row(Modifier.fillMaxSize()) {
//            IconButton(onClick = {
//                if (skinNameCount > 0) {
//                    skinNameCount--
//                    forward = false
//                    onPrevClick()
//                }
//            }) {
//                Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "prev")
//            }
//            AnimatedContent(
//                modifier = Modifier
//                    .weight(1f)
//                    .align(Alignment.CenterVertically),
//                targetState = skinNameCount,
//                transitionSpec = {
//                    val forwardOffset: (fullWidth: Int) -> Int = { it/2 }
//                    val backwardOffset: (fullWidth: Int) -> Int = { -it/2 }
//                    slideInHorizontally(animationSpec = tween(220, delayMillis = 90), initialOffsetX = if (forward) forwardOffset else backwardOffset)
//                        .togetherWith(slideOutHorizontally(animationSpec = tween(90), targetOffsetX = if (forward) backwardOffset else forwardOffset))
//                },
//                label = ""
//            ) { state ->
//                Text(
//                    modifier = Modifier.fillMaxWidth(),
//                    text = detail.getSkinNames(state)?: stringResource(id = R.string.skin_name_default),
//                    textAlign = TextAlign.Center
//                )
//            }
//            IconButton(onClick = {
//                if (skinNameCount < pageSize-1) {
//                    skinNameCount++
//                    forward = true
//                    onNextClick()
//                }
//            }) {
//                Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = "next")
//            }
//        }
//    }
//}

@Composable
fun ChampionSkinsScreen(
    modifier: Modifier = Modifier,
    championDetail: ChampionDetail,
    onItemClick: (SkinNumber) -> Unit
) {
    val skins = championDetail.skins.toMutableStateList()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(skins) { sn ->
            ChampionSkinItem(skinNumber = sn, cd = championDetail, onClick = {
                skins.forEach {
                    it.isSelected = it.num == sn.num
                }
                onItemClick(sn)
            })
        }
    }
}

@Composable
fun ChampionSkinItem(modifier: Modifier = Modifier,
                     skinNumber: SkinNumber,
                     cd: ChampionDetail,
                     onClick: (SkinNumber) -> Unit)
{
//    val checkState by remember { mutableStateOf(isChecked) }
//    val border by remember { mutableStateOf(if (checkState) BorderStroke(width = Dp.Hairline, color = Golden) else null) }
//    val elevation by remember { mutableStateOf(if (checkState) 6.dp else 1.dp) }
    Card(
        modifier = modifier
            .fillMaxWidth(),
        border = if (skinNumber.isSelected) BorderStroke(width = Dp.Hairline, color = Golden) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (skinNumber.isSelected) 6.dp else 1.dp
        )
    ) {
        Box(modifier = Modifier.height(128.dp)) {
            AsyncImage(
                model = cd.getSplash(skinNumber.num),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent, Color(0xff111111)
                                )
                            )
                        )
                    },
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
            Text(
                text = skinNumber.name,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                maxLines = 1
            )
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardDefaults.shape)
                    .clickable {
                        onClick(skinNumber)
                    },
                color = Color.Transparent,
            ) {}
        }
    }
}

@Composable
@Preview
fun PreviewSkins() {
    val detail = ChampionDetail(
        "aatrox",
        listOf(SkinNumber(1, "Justicar Aatrox"), SkinNumber(2, "Mecha Aatrox"), SkinNumber(3, "Sea Hunter Aatrox"), SkinNumber(7, "Blood Moon Aatrox")),
        "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. But after centuries of imprisonment, Aatrox was the first to find freedom once more, corrupting and transforming those foolish enough to try and wield the magical weapon that contained his essence. Now, with stolen flesh, he walks Runeterra in a brutal approximation of his previous form, seeking an apocalyptic and long overdue vengeance.",
        emptyList(),
        Passive(
            "p",
            "Periodically, Aatrox's next basic attack deals bonus <physicalDamage>physical damage</physicalDamage> and heals him, based on the target's max health.",
            Image("")
        )
    )
    MyApplicationTheme {
        ChampionSkinsScreen(championDetail = detail) {

        }
    }
}
