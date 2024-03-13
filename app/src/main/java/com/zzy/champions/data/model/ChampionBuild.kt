package com.zzy.champions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

internal val BUILD_OP_GG = ChampionBuild("OP.GG", "https://www.op.gg/champions/{}/build?region=global")

internal val BUILD_UGG = ChampionBuild("U.GG", "https://u.gg/lol/champions/{}/build")

internal val BUILD_OP_GG_ARAM = ChampionBuild("OP.GG ARAM", "https://www.op.gg/modes/aram/{}/build?region=global")

@Entity
data class ChampionBuild(
    @PrimaryKey var nameOfBuild: String,
    var url: String
) {
    fun getWebUrl(championName: String) = url.replace("{}", championName, true)
}