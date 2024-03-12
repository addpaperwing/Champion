package com.zzy.champions.ui.theme

import androidx.compose.ui.graphics.Color

val FighterRed = Color(0xffd1857a)
val MageBlue = Color(0xff66b0c1)
val AssassinPurple = Color(0xffa89af3)
val SupportGreen = Color(0xff56af9f)
val TankSliver = Color(0xffc8c8c8)
val MarksmanOrange = Color(0xffac7d3f)

internal const val FIGHTER = "Fighter"
internal const val MARKSMAN = "Marksman"
internal const val ASSASSIN = "Assassin"
internal const val MAGE = "Mage"
internal const val SUPPORT = "Support"
internal const val TANK = "Tank"

fun getChampionTagColor(tag: String): Color {
    return when (tag) {
        FIGHTER -> FighterRed
        ASSASSIN -> AssassinPurple
        MAGE -> MageBlue
        MARKSMAN -> MarksmanOrange
        SUPPORT -> SupportGreen
        TANK -> TankSliver
        else -> TankSliver
    }
}

