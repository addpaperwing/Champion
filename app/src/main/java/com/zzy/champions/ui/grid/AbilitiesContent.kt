package com.zzy.champions.ui.grid

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zzy.champions.R
import com.zzy.champions.data.model.Ability
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Abilities(
    modifier: Modifier = Modifier,
    champion: Champion,
    abilities: List<Ability>
) {
    val pagerState = rememberPagerState { abilities.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.padding(top = 16.dp)) {
        AbilitiesIndicator(champion = champion, abilities = abilities, initPage = pagerState.currentPage) { index ->
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
        HorizontalPager(
            modifier = Modifier,
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            Box(modifier = Modifier.graphicsLayer {
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                translationX = pageOffset * size.width
                alpha = 1 - pageOffset.absoluteValue
            }) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = stringArrayResource(id = R.array.abilities)[page],
                        color = Color.LightGray,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                    )
                    Text(
                        text = abilities[page].name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = abilities[page].description,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun AbilitiesIndicator(
    modifier: Modifier = Modifier,
    champion: Champion,
    abilities: List<Ability>,
    activeColor: Color = MaterialTheme.colorScheme.onTertiary,
    initPage: Int,
    onPageChanged: (Int) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(initPage) }

    Row(
        modifier = modifier
            .drawBehind {
                val bottomPadding = 8.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(x = 0f, y = size.height - bottomPadding),
                    end = Offset(x = size.width, y = size.height - bottomPadding),
                    strokeWidth = 0.5f
                )
            }
            .fillMaxWidth()
            .padding(horizontal = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        repeat(abilities.size) { index ->
            AbilityImage(
                modifier = Modifier,
                painter = painterResource(
                    id = when (index) {
                        0 -> R.drawable.aatrox_passive
                        1 -> R.drawable.aatroxq
                        2 -> R.drawable.aatroxw
                        3 -> R.drawable.aatroxe
                        else -> R.drawable.aatroxr
                    }
                ),
                contentDescription = abilities[index].name,
                activeColor = activeColor,
                inactiveColor = Color.White,
                isSelected = selectedIndex == index
            ) {
                selectedIndex = index
                onPageChanged(index)
            }
        }
    }
}

@Composable
fun AbilityImage(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String?,
    activeColor: Color,
    inactiveColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedOffsetDp: Dp by animateDpAsState(
        targetValue = if (isSelected) 0.dp else 16.dp,
        label = "image offset"
    )
    val animatedScale: Float by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "circle scale"
    )
    val dotColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "dot color"
    )

    val imageRaisedHeight = 16
    val imageSize = 48
    val composableHeight = imageRaisedHeight * 2 + imageSize

    val strokeWidth = 1
    val circleSize = 10.dp
    val innerCircle = 6.dp

    Column(modifier = modifier
        .clickable {
            onClick()
        }
        .height(composableHeight.dp)) {
        Box(
            modifier = Modifier
                .offset(0.dp, animatedOffsetDp)
        ) {
            Image(
                modifier = Modifier
                    .size(imageSize.dp)
                    .padding(3.dp),
                painter = painter,
                contentDescription = contentDescription
            )
            if (isSelected) {
                Box(
                    Modifier
                        .size(imageSize.dp)
                        .border(
                            border = BorderStroke(
                                0.5.dp,
                                Brush.horizontalGradient(listOf(activeColor, activeColor))
                            ),
                            shape = CutCornerShape(topEnd = (imageSize / 4).dp)
                        )
                ) {

                }
            }
        }
        Canvas(modifier = Modifier.padding(top = imageRaisedHeight.dp)) {
//            val scale = 0.2f.coerceAtLeast(1 - pageOffsetFractionAbsoluteValue)
            val outlineStroke = Stroke(strokeWidth.dp.toPx())

            val x = imageSize.dp.toPx() / 2
            val y = imageRaisedHeight.dp.toPx() / 2
            val circleCenter = Offset(x, y)
            val innerRadius = innerCircle.toPx() / 2
            val radius = (circleSize.toPx() * animatedScale) / 2

            if (isSelected) {
                drawLine(
                    color = activeColor,
                    start = Offset(x, y - radius),
                    end = Offset(x, 0f - y * 2),
                    strokeWidth = strokeWidth.dp.toPx()
                )
            }

            drawCircle(
                color = dotColor,
                center = circleCenter,
                radius = innerRadius
            )

            drawCircle(
                color = activeColor,
                style = outlineStroke,
                center = circleCenter,
                radius = radius
            )
        }
    }
}

@Preview
@Composable
fun PreviewAbilities() {
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
    val abilities = listOf(
        Ability(
        "p",
        "Periodically, Aatrox's next basic attack deals bonus <physicalDamage>physical damage</physicalDamage> and heals him, based on the target's max health."
    ),
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
    )

    MyApplicationTheme {
        Abilities(champion = champion, abilities = abilities)
    }
}