package com.zzy.champions

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Stats

object AndroidTestUtil {

    fun createChampion(
        name: String = "Aatrox",
        title: String = "the Darkin Blade",
        image: Image = Image(""),
        tags: List<String> = emptyList(),
        partype: String = "",
        info: Info = Info(),
        stats: Stats = Stats(),
    ): Champion = Champion(name, name, title, image, tags, partype, info, stats)

    fun createChampionDetail(
        id: String,
        skins: List<SkinNumber> = emptyList()
    ): ChampionDetail = ChampionDetail(
        championId = id,
        skins = skins,
        lore = "",
        spells = emptyList(),
        passive = Passive(
            name = "", description = "", image = Image(full = "")
        )
    )
}