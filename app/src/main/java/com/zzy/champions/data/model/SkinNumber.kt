package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SkinNumber(
    val num: Int,
    val name: String,
    val parentSkin: Int? = null,
) {
    val isChroma: Boolean get() = parentSkin != null

    fun getSplash(championId: String): String = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${championId}_$num.jpg"
}