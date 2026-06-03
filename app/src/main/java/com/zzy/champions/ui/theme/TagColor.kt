package com.zzy.champions.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.zzy.champions.R

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

private data class TagDisplay(val color: Color, @param:StringRes val nameRes: Int)

private fun tagDisplay(tag: String): TagDisplay = when (tag) {
    FIGHTER -> TagDisplay(FighterRed, R.string.tag_fighter)
    ASSASSIN -> TagDisplay(AssassinPurple, R.string.tag_assassin)
    MAGE -> TagDisplay(MageBlue, R.string.tag_mage)
    MARKSMAN -> TagDisplay(MarksmanOrange, R.string.tag_marksman)
    SUPPORT -> TagDisplay(SupportGreen, R.string.tag_support)
    TANK -> TagDisplay(TankSliver, R.string.tag_tank)
    else -> TagDisplay(TankSliver, R.string.tag_tank)
}

fun getChampionTagColor(tag: String): Color = tagDisplay(tag).color

@StringRes
fun getTagStringRes(tag: String): Int = tagDisplay(tag).nameRes

