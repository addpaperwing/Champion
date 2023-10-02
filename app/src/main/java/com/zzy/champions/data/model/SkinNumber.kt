package com.zzy.champions.data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SkinNumber(
    val num: Int,
    val name: String,
    var initSelectState: Boolean = num == 0
) {
    var isSelected by mutableStateOf(initSelectState)
}