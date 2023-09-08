package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SkinNumber(val num: Int) {

    fun getSplash(championId: String) = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${championId}_${num}.jpg"
}