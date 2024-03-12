package com.zzy.champions.ui

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.Stats

object TestUtil {

    internal val LOCAL_CHAMP_NAME = "local"
    internal val REMOTE_CHAMP_NAME = "remote"

    fun aatrox(remote: Boolean = false): Champion = Champion(
        "Aatrox",
        if (remote) REMOTE_CHAMP_NAME else LOCAL_CHAMP_NAME,
        "the Darkin Blade",
        Image(""),
        emptyList(),
        "",
        Info(),
        Stats()
    )

    fun createChampion(name: String = "Aatrox"): Champion = Champion(
        name,
        name,
        "the Darkin Blade",
        Image(""),
        emptyList(),
        "",
        Info(),
        Stats()
    )

    fun createChampionDetail(id: String): ChampionDetail = ChampionDetail(
        championId = id,
        skins = emptyList(),
        lore = "",
        spells = emptyList(),
        passive = Passive(
            name = "", description = "", image = Image(full = "")
        )
    )
}