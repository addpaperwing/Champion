package com.zzy.champions.ui.grid

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R
import com.zzy.champions.data.model.Ability
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun BlurBanner(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.splash_aatrox_0),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1215 / 717f)
//                .blur(2.dp)
        )
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
fun GeneralInfo(modifier: Modifier = Modifier, champion: Champion, championDetail: ChampionDetail) {
    var nameSize by remember { mutableStateOf(IntSize.Zero) }
    Column(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .drawBehind {
                val titleHeight = 10.sp.toPx()
                val distanceToHorizontalEdge = (size.width - nameSize.width) / 2
                val distanceToTopEdge = nameSize.height / 2 + titleHeight

                drawLine(
                    color = Color.White,
                    start = Offset(x = distanceToHorizontalEdge, y = distanceToTopEdge),
                    end = Offset(x = 0f, y = distanceToTopEdge),
                    strokeWidth = 0.5f
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = distanceToTopEdge),
                    end = Offset(x = 0f, y = size.height),
                    strokeWidth = 0.5f
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = size.height),
                    end = Offset(x = size.width, y = size.height),
                    strokeWidth = 0.5f
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = size.width, y = size.height),
                    end = Offset(x = size.width, y = distanceToTopEdge),
                    strokeWidth = 0.5f
                )

                drawLine(
                    color = Color.White,
                    start = Offset(x = size.width, y = distanceToTopEdge),
                    end = Offset(x = size.width - distanceToHorizontalEdge, y = distanceToTopEdge),
                    strokeWidth = 0.5f
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = champion.title.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp
        )
        Text(
            text = champion.name.uppercase(),
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
                text = champion.tags.reduce { acc, s -> acc.plus(" · $s") }.uppercase(),
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 12.sp
            )
            ExpandableText(
                text = championDetail.lore,
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
    infoContent: @Composable PagerScope.(page: Int) -> Unit = {},
    abilitiesContent: @Composable PagerScope.(page: Int) -> Unit = {},
    statsContent: @Composable PagerScope.(page: Int) -> Unit = {},
    buildContent: @Composable PagerScope.(page: Int) -> Unit = {}
    ) {
    val titles = stringArrayResource(id = R.array.tabs)
    val pagerState = rememberPagerState { titles.size }
    val scope = rememberCoroutineScope()

    Column(modifier.padding(bottom = 64.dp)) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
//                if (pagerState.currentPage < tabPositions.size) {
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
//                }
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
        HorizontalPager(state = pagerState) { page ->
            when(page) {
                0 -> infoContent(page)
                1 -> abilitiesContent(page)
                2 -> statsContent(page)
                3 -> buildContent(page)
            }
        }
    }
}

//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun Abilities(
//    modifier: Modifier = Modifier,
//    champion: Champion,
//    abilities: List<Ability>
//) {
//    val pagerState = rememberPagerState { abilities.size }
//    Column(modifier = modifier) {
//        AbilitiesIndicator(champion = champion, abilities = abilities) { index ->
//            pagerState.animateScrollToPage(index)
//        }
//        HorizontalPager(
//            modifier = Modifier,
//            state = pagerState,
//            userScrollEnabled = false
//        ) { page ->
//            Box(modifier = Modifier.graphicsLayer {
//                val pageOffset =
//                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
//                translationX = pageOffset * size.width
//                alpha = 1 - pageOffset.absoluteValue
//            }) {
//                Column(
//                    modifier = Modifier
//                        .align(Alignment.TopCenter)
//                        .fillMaxWidth()
//                        .padding(all = 16.dp)
//                ) {
//                    Text(
//                        text = stringArrayResource(id = R.array.abilities)[page],
//                        color = Color.LightGray,
//                        fontSize = 8.sp,
//                        lineHeight = 10.sp,
//                    )
//                    Text(
//                        text = abilities[page].name,
//                        color = MaterialTheme.colorScheme.onPrimary,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = abilities[page].description,
//                        color = MaterialTheme.colorScheme.onPrimary,
//                        fontSize = 12.sp,
//                        lineHeight = 14.sp,
//                        minLines = 6
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AbilitiesIndicator(
//    modifier: Modifier = Modifier,
//    champion: Champion,
//    abilities: List<Ability>,
//    activeColor: Color = MaterialTheme.colorScheme.onTertiary,
//    onPageChanged: suspend (Int) -> Unit
//) {
//    var selectedIndex by remember { mutableIntStateOf(0) }
//    LaunchedEffect(selectedIndex) {
//        onPageChanged(selectedIndex)
//    }
//
//    Row(
//        modifier = modifier
//            .drawBehind {
//                val bottomPadding = 8.dp.toPx()
//                drawLine(
//                    color = Color.White,
//                    start = Offset(x = 0f, y = size.height - bottomPadding),
//                    end = Offset(x = size.width, y = size.height - bottomPadding),
//                    strokeWidth = 0.5f
//                )
//            }
//            .fillMaxWidth()
//            .padding(horizontal = 36.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//
//        repeat(abilities.size) { index ->
//            AbilityImage(
//                modifier = Modifier,
//                painter = painterResource(
//                    id = when (index) {
//                        0 -> R.drawable.aatrox_passive
//                        1 -> R.drawable.aatroxq
//                        2 -> R.drawable.aatroxw
//                        3 -> R.drawable.aatroxe
//                        else -> R.drawable.aatroxr
//                    }
//                ),
//                contentDescription = abilities[index].name,
//                activeColor = activeColor,
//                inactiveColor = Color.White,
//                isSelected = selectedIndex == index
//            ) {
//                selectedIndex = index
//            }
//        }
//    }
//}
//
//@Composable
//fun AbilityImage(
//    modifier: Modifier = Modifier,
//    painter: Painter,
//    contentDescription: String?,
//    activeColor: Color,
//    inactiveColor: Color,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    val animatedOffsetDp: Dp by animateDpAsState(
//        targetValue = if (isSelected) 0.dp else 16.dp,
//        label = "image offset"
//    )
//    val animatedScale: Float by animateFloatAsState(
//        targetValue = if (isSelected) 1f else 0f,
//        label = "circle scale"
//    )
//    val dotColor by animateColorAsState(
//        targetValue = if (isSelected) activeColor else inactiveColor,
//        label = "dot color"
//    )
//
//    val imageRaisedHeight = 16
//    val imageSize = 48
//    val composableHeight = imageRaisedHeight * 2 + imageSize
//
//    val strokeWidth = 1
//    val circleSize = 10.dp
//    val innerCircle = 6.dp
//
//    Column(modifier = modifier
//        .clickable {
//            onClick()
//        }
//        .height(composableHeight.dp)) {
//        Box(
//            modifier = Modifier
//                .offset(0.dp, animatedOffsetDp)
//        ) {
//            Image(
//                modifier = Modifier
//                    .size(imageSize.dp)
//                    .padding(3.dp),
//                painter = painter,
//                contentDescription = contentDescription
//            )
//            if (isSelected) {
//                Box(
//                    Modifier
//                        .size(imageSize.dp)
//                        .border(
//                            border = BorderStroke(
//                                0.5.dp,
//                                Brush.horizontalGradient(listOf(activeColor, activeColor))
//                            ),
//                            shape = CutCornerShape(topEnd = (imageSize / 4).dp)
//                        )
//                ) {
//
//                }
//            }
//        }
//        Canvas(modifier = Modifier.padding(top = imageRaisedHeight.dp)) {
////            val scale = 0.2f.coerceAtLeast(1 - pageOffsetFractionAbsoluteValue)
//            val outlineStroke = Stroke(strokeWidth.dp.toPx())
//
//            val x = imageSize.dp.toPx() / 2
//            val y = imageRaisedHeight.dp.toPx() / 2
//            val circleCenter = Offset(x, y)
//            val innerRadius = innerCircle.toPx() / 2
//            val radius = (circleSize.toPx() * animatedScale) / 2
//
//            if (isSelected) {
//                drawLine(
//                    color = activeColor,
//                    start = Offset(x, y - radius),
//                    end = Offset(x, 0f - y * 2),
//                    strokeWidth = strokeWidth.dp.toPx()
//                )
//            }
//
//            drawCircle(
//                color = dotColor,
//                center = circleCenter,
//                radius = innerRadius
//            )
//
//            drawCircle(
//                color = activeColor,
//                style = outlineStroke,
//                center = circleCenter,
//                radius = radius
//            )
//        }
//    }
//}

//@Composable
//fun ChampionLevel(modifier: Modifier = Modifier, champion: Champion) {
//    Column(modifier = modifier) {
//        Row {
//            Text(
//                text = stringResource(id = R.string.level),
//                color = MaterialTheme.colorScheme.onPrimary,
//                modifier = Modifier,
//                fontSize = 24.sp
//            )
//            Text(
//                text = "18",
//                color = MaterialTheme.colorScheme.onPrimary,
//                modifier = Modifier,
//                fontSize = 36.sp
//            )
//        }
//        Row {
//            BaseStatsText(
//                text = stringResource(
//                    id = R.string.move_speed,
//                    champion.stats.movespeed
//                )
//            )
//            BaseStatsText(
//                text = stringResource(
//                    id = R.string.attack_range,
//                    champion.stats.attackrange
//                )
//            )
//        }
//    }
//}
//
//@Composable
//fun BaseStatsText(text: String) {
//    Text(
//        text = text,
//        color = MaterialTheme.colorScheme.onPrimary,
//        modifier = Modifier
//            .border(
//                border = BorderStroke(0.5.dp, Color.White),
//                shape = CutCornerShape(topEnd = 6.dp, bottomStart = 6.dp)
//            )
//            .padding(6.dp),
//        fontSize = 12.sp
//    )
//}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChampionDetailScreen(modifier: Modifier = Modifier, champion: Champion, detail: ChampionDetail) {
    Scaffold { padding ->
        Column(modifier.padding(padding)) {
            Box(Modifier.aspectRatio(1f / 1f)) {
                BlurBanner(Modifier.align(Alignment.TopCenter))
                GeneralInfo(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    champion = champion,
                    championDetail = detail
                )
            }
            TabPagerSection(
                abilitiesContent = {
                    Abilities(
                        modifier = Modifier,
                        champion = champion,
                        abilities = detail.getAbilities()
                    )
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewDetailScreen() {
    val champion = Champion(
        "aatrox",
        1,
        "star guardian seraphine The Darkin Blade",
        "The Darkin Blade",
        listOf("Warrior", "Fighter", "Assassin"),
        "Blood Well",
        Info(difficulty = 5),
        Stats(movespeed = BigDecimal(355), attackrange = BigDecimal(120))
    )
    val detail = ChampionDetail(
        "aatrox",
        emptyList(),
        "Once honored defenders of Shurima against the Void, Aatrox and his brethren would eventually become an even greater threat to Runeterra, and were defeated only by cunning mortal sorcery. But after centuries of imprisonment, Aatrox was the first to find freedom once more, corrupting and transforming those foolish enough to try and wield the magical weapon that contained his essence. Now, with stolen flesh, he walks Runeterra in a brutal approximation of his previous form, seeking an apocalyptic and long overdue vengeance.",
        listOf(
            Ability(
                "q",
                "Aatrox slams his greatsword down, dealing physical damage. He can swing three times, each with a different area of effect."
            ),
            Ability(
                "w",
                "Aatrox smashes the ground, dealing damage to the first enemy hit. Champions and large monsters have to leave the impact area quickly or they will be dragged to the center and take the damage again."
            ),
            Ability(
                "e",
                "Passively, Aatrox heals when damaging enemy champions. On activation, he dashes in a direction."
            ),
            Ability(
                "r",
                "Aatrox unleashes his demonic form, fearing nearby enemy minions and gaining attack damage, increased healing, and Move Speed. If he gets a takedown, this effect is extended."
            )
        ),
        Ability(
            "p",
            "Periodically, Aatrox's next basic attack deals bonus <physicalDamage>physical damage</physicalDamage> and heals him, based on the target's max health."
        )
    )
    MyApplicationTheme {
        ChampionDetailScreen(champion = champion, detail = detail)
    }
}
