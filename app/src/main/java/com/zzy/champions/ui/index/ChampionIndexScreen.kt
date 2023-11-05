package com.zzy.champions.ui.index


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.components.ChampionCard
import com.zzy.champions.ui.components.Header
import com.zzy.champions.ui.components.PREDICTION_ITEM_HEIGHT
import com.zzy.champions.ui.components.PredictionSearchBar
import com.zzy.champions.ui.components.SUPPORT
import com.zzy.champions.ui.components.TANK
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun ChampionIndex(
    modifier: Modifier = Modifier,
    predictions: List<String>,
    onTextChanged: (String) -> Unit,
    onDoneActionClick: (String) -> Unit,
    onPredictionClick: (String) -> Unit,
    champions: List<Champion>,
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Header(onSettingClick = onSettingClick)
        Box {
            LazyVerticalGrid(
                modifier = Modifier.padding(top = PREDICTION_ITEM_HEIGHT + 12.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(count = champions.size) { index ->
                    ChampionCard(champion = champions[index]) {
                        onItemClick(champions[index])
                    }
                }
            }
            PredictionSearchBar(
                modifier = Modifier,
                predictions = predictions,
                onTextChanged = onTextChanged,
                onDoneActionClick = onDoneActionClick,
                onPredictionClick = onPredictionClick
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewChampionIndex() {
    val aatrox = Champion(
        "Aatrox",
        "Aatrox",
        "the Darkin Blade",
        Image(""),
        listOf(TANK, SUPPORT),
        "",
        Info(difficulty = 7),
        Stats()
    )
    MyApplicationTheme {
        val champions: ArrayList<Champion> = ArrayList()

        for (i in 0..10) {
            champions.add(aatrox)
        }
        Scaffold { padding ->
            ChampionIndex(
                modifier = Modifier.padding(padding),
                predictions = emptyList(),
                onTextChanged = {},
                onDoneActionClick = {},
                onPredictionClick = {},
                champions = champions,
                onSettingClick = {

                },
                onItemClick = {

            })
        }
    }
}