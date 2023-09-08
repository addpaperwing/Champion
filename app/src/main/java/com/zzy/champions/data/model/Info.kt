package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Info(
    val attack: Int = 0,
    val defense: Int = 0,
    val magic: Int = 0,
    val difficulty: Int = 0
) {
}