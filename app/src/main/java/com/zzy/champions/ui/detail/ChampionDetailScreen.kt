package com.zzy.champions.ui.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zzy.champions.R
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.abilities.Abilities
import com.zzy.champions.ui.builds.ChampionBuildScreen
import com.zzy.champions.ui.info.InfoPie
import com.zzy.champions.ui.stats.ChampionStats
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Banner(modifier: Modifier = Modifier, detail: ChampionDetail) {
    Box(modifier = modifier) {
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1215 / 717f),
//            userScrollEnabled = false
//        ) { page ->
//            Column(Modifier.fillMaxHeight()) {
                AsyncImage(
                    model = detail.getSplash(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1215 / 717f)
                )
//            }
//        }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .aspectRatio(1215 / 717f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent, MaterialTheme.colorScheme.primary
                        )
                    )
                )
        )
    }
}

@Composable
fun GeneralInfo(modifier: Modifier = Modifier, title: String, name: String, tags: List<String>, lore: String) {
    var nameSize by remember { mutableStateOf(IntSize.Zero) }
    Column(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .drawBehind {
                val titleHeight = 10.sp.toPx()
                val distanceToHorizontalEdge = (size.width - nameSize.width) / 2
                val distanceToTopEdge = nameSize.height / 2 + titleHeight
                val strokeWidth = Dp.Hairline.toPx()

                drawLine(
                    color = Color.White,
                    start = Offset(x = distanceToHorizontalEdge, y = distanceToTopEdge),
                    end = Offset(x = 0f, y = distanceToTopEdge),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = distanceToTopEdge),
                    end = Offset(x = 0f, y = size.height),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = size.height),
                    end = Offset(x = size.width, y = size.height),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = size.width, y = size.height),
                    end = Offset(x = size.width, y = distanceToTopEdge),
                    strokeWidth = strokeWidth
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = size.width, y = distanceToTopEdge),
                    end = Offset(x = size.width - distanceToHorizontalEdge, y = distanceToTopEdge),
                    strokeWidth = strokeWidth
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp
        )
        Text(
            text = name.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .onSizeChanged { size ->
                    nameSize = size
                },
            textAlign = TextAlign.Center,
            fontSize = 36.sp,
            lineHeight = 38.sp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tags.reduce { acc, s -> acc.plus(" · $s") }.uppercase(),
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 12.sp
            )
            ExpandableText(
                text = lore,
                modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    maxLinesWhenCollapsed: Int = 3,
    expandButtonText: String = stringResource(id = R.string.more),
    collapsedButtonText: String = stringResource(id = R.string.less),
    text: String
) {
    var expanded by remember { mutableStateOf(false) }
    var clickable by remember { mutableStateOf(false) }
    var lastCharIndex by remember { mutableIntStateOf(0) }

    val actionTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.onTertiary,
        fontWeight = FontWeight.Bold
    )
    Text(
        modifier = modifier
            .clickable {
                expanded = !expanded
            }
            .animateContentSize(),
        text = buildAnnotatedString {
            if (clickable) {
                if (expanded) {
                    append(text)
                    withStyle(actionTextStyle) { append(collapsedButtonText.uppercase()) }
                } else {
                    val adjustText = text.substring(0, lastCharIndex)
                        .dropLast(expandButtonText.length)
                        .dropLastWhile { Character.isWhitespace(it) || it == '.' }
                    append(adjustText)
                    withStyle(actionTextStyle) { append(expandButtonText.uppercase()) }
                }
            } else {
                append(text)
            }
        },
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = if (expanded) Int.MAX_VALUE else maxLinesWhenCollapsed,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        onTextLayout = { layoutResult ->
            if (!expanded && layoutResult.hasVisualOverflow) {
                clickable = true
                lastCharIndex = layoutResult.getLineEnd(maxLinesWhenCollapsed - 1)
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabPagerSection(
    modifier: Modifier = Modifier,
    infoContent: @Composable PagerScope.() -> Unit = {},
    abilitiesContent: @Composable PagerScope.() -> Unit = {},
    statsContent: @Composable PagerScope.() -> Unit = {},
    buildContent: @Composable PagerScope.() -> Unit = {}
    ) {
    val titles = stringArrayResource(id = R.array.tabs)
    val pagerState = rememberPagerState { titles.size }
    val scope = rememberCoroutineScope()

    Column(modifier.padding(bottom = 64.dp)) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onTertiary,
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
                0 -> infoContent()
                1 -> abilitiesContent()
                2 -> statsContent()
                3 -> buildContent()
            }
        }
    }
}

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChampionDetail(modifier: Modifier = Modifier, champion: Champion, detail: ChampionDetail) {
//    val pagerState = rememberPagerState { detail.skins.size + 1 }
//    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

//    Box(modifier = modifier) {
        Column(
            modifier
                .verticalScroll(scrollState),
        ) {
            Box(Modifier.aspectRatio(1f / 1f)) {
                Banner(Modifier.align(Alignment.TopCenter), detail)
                GeneralInfo(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    title = champion.title,
                    name = champion.name,
                    tags = champion.tags,
                    lore = detail.lore
                )
            }
            TabPagerSection(
                infoContent = {
                    InfoPie(
                        info = champion.info,
                        bMoveSpeed = champion.stats.movespeed,
                        bAttackRange = champion.stats.attackrange
                    )
                },
                abilitiesContent = {
                    Abilities(
                        modifier = Modifier.fillMaxHeight(),
                        abilities = detail.getAbilities()
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
//        SwitchSkinButton(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(all = 16.dp),
//            detail,
//            pagerState.pageCount,
//            onPrevClick = {
//                if (pagerState.currentPage > 0) {
//                    scope.launch {
//                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
//                    }
//                }
//            },
//            onNextClick = {
//                if (pagerState.currentPage < pagerState.pageCount)
//                    scope.launch {
//                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
//                    }
//            }
//        )
//    }
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
        Scaffold { padding ->
            ChampionDetail(modifier = Modifier.padding(padding), champion = champion, detail = detail)
        }
    }
}
