package com.zzy.champions.data.model

abstract class Ability(val name: String, val description: String, val image: Image) {

    abstract val type: String
    fun getAbilityImage(version: String) = "$DDRAGON_CDN/$version/img/$type/${image.full}"
}