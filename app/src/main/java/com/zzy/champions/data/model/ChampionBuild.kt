package com.zzy.champions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

internal const val NAME_OF_BUILD_OPGG = "OP.GG"
internal const val URL_OF_OPGG = "https://www.op.gg/champions/{}/build?region=global"
internal const val NAME_OF_BUILD_UGG = "U.GG"
internal const val URL_OF_UGG = "https://u.gg/lol/champions/{}/build"
internal const val NAME_OF_BUILD_OPGG_ARAM = "OP.GG ARAM"
internal const val URL_OF_OPGG_ARAM = "https://www.op.gg/modes/aram/{}/build?region=global"

@Entity
data class ChampionBuild(
    var nameOfBuild: String,
    var url: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    fun getWebUrl(championName: String) = url.replace("{}", championName, true)
}