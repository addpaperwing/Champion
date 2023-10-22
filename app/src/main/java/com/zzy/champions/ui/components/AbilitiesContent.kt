package com.zzy.champions.ui.components

import android.widget.TextView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import com.zzy.champions.R
import com.zzy.champions.data.model.Ability
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.Spell
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Abilities(
    modifier: Modifier = Modifier,
    abilities: List<Ability>
) {
    val pagerState = rememberPagerState { abilities.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.padding(top = 16.dp)) {
        AbilitiesIndicator(abilities = abilities, initPage = pagerState.currentPage) { index ->
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
        HorizontalPager(
            modifier = Modifier,
            state = pagerState,
            userScrollEnabled = false,
            verticalAlignment = Alignment.Top
        ) { page ->
            Box(modifier = Modifier.graphicsLayer {
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                translationX = pageOffset * size.width
                alpha = 1 - pageOffset.absoluteValue
            }) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = stringArrayResource(id = R.array.abilities)[page],
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                    )
                    Text(
                        text = abilities[page].name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    AndroidView(
                        modifier = modifier,
                        factory = { context ->
                            TextView(context).apply {
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                            }

                        },
                        update = {
                            it.text = HtmlCompat.fromHtml(
                                abilities[page].description,
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AbilitiesIndicator(
    modifier: Modifier = Modifier,
    abilities: List<Ability>,
    activeColor: Color = MaterialTheme.colorScheme.tertiary,
    initPage: Int,
    onPageChanged: (Int) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(initPage) }

    val lineColor = MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = modifier
            .drawBehind {
                val bottomPadding = 8.dp.toPx()
                drawLine(
                    color = lineColor,
                    start = Offset(x = 0f, y = size.height - bottomPadding),
                    end = Offset(x = size.width, y = size.height - bottomPadding),
                    strokeWidth = Dp.Hairline.toPx()
                )
            }
            .fillMaxWidth()
            .padding(horizontal = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        repeat(abilities.size) { index ->
            AbilityImage(
                modifier = Modifier,
                model = abilities[index].getAbilityImage(Champion.version),
                contentDescription = abilities[index].name,
                activeColor = activeColor,
                inactiveColor = MaterialTheme.colorScheme.onBackground,
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
    model: String,
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
            AsyncImage(
                modifier = Modifier
                    .size(imageSize.dp)
                    .padding(3.dp),
                model = model,
                contentDescription = contentDescription
            )
            if (isSelected) {
                Box(
                    Modifier
                        .size(imageSize.dp)
                        .border(
                            border = BorderStroke(
                                Dp.Hairline,
                                Brush.horizontalGradient(listOf(activeColor, activeColor))
                            ),
                            shape = CutCornerShape(topEnd = (imageSize / 4).dp)
                        )
                )
            }
        }
        Canvas(modifier = Modifier.padding(top = imageRaisedHeight.dp)) {
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
                    strokeWidth = Dp.Hairline.toPx()
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
        "star guardian seraphine The Darkin Blade",
        "The Darkin Blade",
        Image(""),
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
            Spell(
                "q",
                "Aatrox slams his greatsword down, dealing physical damage. He can swing three times, each with a different area of effect.",
                Image("")
            ),
            Spell(
                "w",
                "Aatrox smashes the ground, dealing damage to the first enemy hit. Champions and large monsters have to leave the impact area quickly or they will be dragged to the center and take the damage again.",
                Image("")
            ),
            Spell(
                "e",
                "Passively, Aatrox heals when damaging enemy champions. On activation, he dashes in a direction.",
                Image("")
            ),
            Spell(
                "r",
                "Aatrox unleashes his demonic form, fearing nearby enemy minions and gaining attack damage, increased healing, and Move Speed. If he gets a takedown, this effect is extended.",
                Image("")
            )
        ),
        Passive(
            "p",
            "Periodically, Aatrox's next basic attack deals bonus <physicalDamage>physical damage</physicalDamage> and heals him, based on the target's max health.",
            Image("")
        )
    )

    MyApplicationTheme {
        Abilities(abilities = detail.getAbilities())
    }
}