package com.zzy.champions.data.model

abstract class Ability(val name: String, val description: String, val image: Image) {

    abstract val type: String
    fun getAbilityImage(version: String = "3.9.7") = "https://ddragon.leagueoflegends.com/cdn/$version/img/$type/${image.full}"
}