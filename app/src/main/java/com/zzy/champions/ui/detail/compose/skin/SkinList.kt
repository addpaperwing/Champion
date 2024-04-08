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
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Passive
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
        SkinList(
            championId = detail.championId,
            state = rememberSkinListState(),
            skins = detail.skins,
            onItemClick = {

            }
        )
    }
}
