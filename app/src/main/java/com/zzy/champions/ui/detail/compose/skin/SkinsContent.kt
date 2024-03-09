package com.zzy.champions.ui.detail.compose.skin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.ui.theme.MyApplicationTheme

@Composable
fun SkinsContent(
    modifier: Modifier = Modifier,
    championDetail: ChampionDetail,
    onItemClick: (SkinNumber) -> Unit
) {
    val skins = championDetail.skins.toMutableStateList()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(skins) { sn ->
            SkinItem(skinNumber = sn, cd = championDetail, onClick = {
                skins.forEach {
                    it.isSelected = it.num == sn.num
                }
                onItemClick(sn)
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
        SkinsContent(championDetail = detail) {

        }
    }
}
