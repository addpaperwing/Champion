package com.zzy.champions.data.model

internal const val DDRAGON_CDN = "https://ddragon.leagueoflegends.com/cdn"

fun itemIconUrl(version: String, itemId: String) =
    "$DDRAGON_CDN/$version/img/item/$itemId.png"
