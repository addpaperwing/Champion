package com.zzy.champions.data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class SettingSelectable(
    val name: String,
    val initSelectState: Boolean
) {
    var isSelected by mutableStateOf(initSelectState)
}