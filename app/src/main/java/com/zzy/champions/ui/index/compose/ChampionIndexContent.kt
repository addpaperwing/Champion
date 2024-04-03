package com.zzy.champions.ui.index.compose


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats
import com.zzy.champions.ui.theme.MyApplicationTheme
import com.zzy.champions.ui.theme.SUPPORT
import com.zzy.champions.ui.theme.TANK

@Composable
fun ChampionIndex(
    modifier: Modifier = Modifier,
    searchText: String,
    onTextChanged: (String) -> Unit,
    onDoneActionClick: (String) -> Unit,
    onClearSearchText: () -> Unit,
    champions: List<Champion>,
    onItemClick: (Champion) -> Unit,
    onSettingClick: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)) {
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
            SearchTextField(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = searchText,
                onTextChanged = onTextChanged,
                onClearText = onClearSearchText,
                onDone = {
                    keyboardController?.hide()
                    onDoneActionClick(it)
                }
            )
//            PredictionSearchBar(
//                modifier = Modifier,
//                predictions = predictions,
//                onTextChanged = onTextChanged,
//                onDoneActionClick = onDoneActionClick,
//                onPredictionClick = onPredictionClick,
//                getDisplayText = { champion -> champion.name }
//            )
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
        val text by rememberSaveable { mutableStateOf("") }

        for (i in 0..10) {
            champions.add(aatrox)
        }
        Scaffold { padding ->
            ChampionIndex(
                modifier = Modifier.padding(padding),
                searchText = text,
                onTextChanged = {},
                onClearSearchText = {},
                onDoneActionClick = {},
                champions = champions,
                onSettingClick = {

                },
                onItemClick = {

            })
        }
    }
}