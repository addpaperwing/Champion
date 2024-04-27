package com.zzy.champions.ui.detail.compose.ability

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zzy.champions.R
import com.zzy.champions.data.local.ChampionAndDetailPreviewParameterProvider
import com.zzy.champions.data.model.Ability
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Abilities(
    modifier: Modifier = Modifier,
    version: String,
    abilities: List<Ability>
) {
    val pagerState = rememberPagerState { abilities.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.padding(top = 16.dp).semantics { contentDescription = "Champion abilities" }) {
        AbilitiesIndicator(
            version = version, abilities = abilities, initPage = pagerState.currentPage) { index ->
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
                AbilityText(
                    modifier = Modifier.fillMaxSize().semantics { contentDescription = "ability content $page" },
                    type = stringArrayResource(id = R.array.abilities)[page],
                    name = abilities[page].name,
                    description = abilities[page].description
                )
            }
        }
    }
}

@Composable
fun AbilitiesIndicator(
    modifier: Modifier = Modifier,
    version: String,
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
                modifier = Modifier.semantics { contentDescription = "ability icon $index" },
                model = abilities[index].getAbilityImage(version),
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



@Preview
@Composable
fun PreviewAbilities(
    @PreviewParameter(ChampionAndDetailPreviewParameterProvider::class)
    championAndDetail: ChampionAndDetail
) {
    MyApplicationTheme {
        Abilities(version = "3.3.3", abilities = championAndDetail.detail.getAbilities())
    }
}