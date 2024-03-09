package com.zzy.champions.ui.detail.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChampionDetailTabPager(
    modifier: Modifier = Modifier,
    titles: Array<String>,
    onTabClick: (Int) -> Unit,
    content: @Composable PagerScope.(Int) -> Unit,
) {
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
            content(page)
        }
    }
}