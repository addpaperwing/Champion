package com.zzy.champions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class ChampionBuild(
    val nameOfBuild: String,
    val url: String
) {

    @PrimaryKey(autoGenerate = true) var id: Int = 0
    fun getWebUrl(championName: String) = url.replace("{}", championName, true)
}