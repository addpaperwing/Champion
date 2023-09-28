package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Passive(name: String, description: String, image: Image): Ability(name, description, image) {

    override val type: String = "passive"
}