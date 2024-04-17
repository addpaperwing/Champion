package com.zzy.champions.ui.detail.compose.skin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.local.ChampionAndDetailPreviewParameterProvider
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun rememberSkinListState(initSelection: Int = 0) = remember { SkinListState(initSelection) }
class SkinListState(initSelection: Int) {
    var selection by mutableIntStateOf(initSelection)
}

@Composable
fun SkinList(
    modifier: Modifier = Modifier,
    state: SkinListState,
    championId: String,
    skins: List<SkinNumber>,
    onItemClick: (SkinNumber) -> Unit
) {
    LazyColumn(
        modifier = modifier.semantics { contentDescription = "Champion skins" },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(skins) { skin ->
            SkinItem(skinNumber = skin, championId = championId, isSelected = state.selection == skin.num, onClick = {
                state.selection = skin.num
                onItemClick(skin)
            })
        }
    }
}

@Composable
@Preview
fun PreviewSkins(
    @PreviewParameter(ChampionAndDetailPreviewParameterProvider::class)
    championAndDetail: ChampionAndDetail
) {
    MyApplicationTheme {
        SkinList(
            championId = championAndDetail.detail.championId,
            state = rememberSkinListState(),
            skins = championAndDetail.detail.skins,
            onItemClick = {

            }
        )
    }
}
