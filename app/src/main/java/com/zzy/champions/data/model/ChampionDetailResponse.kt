package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChampionDetailResponse(val data: Map<String, ChampionDetail>)