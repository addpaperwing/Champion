package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChampionResponse(val data: Map<String, Champion>)