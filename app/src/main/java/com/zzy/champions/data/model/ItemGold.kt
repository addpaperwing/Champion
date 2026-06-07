package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemGold(
    val base: Int = 0,
    val purchasable: Boolean = true,
    val total: Int = 0,
    val sell: Int = 0,
)
