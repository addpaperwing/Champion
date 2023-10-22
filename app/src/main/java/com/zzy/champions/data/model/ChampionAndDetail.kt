package com.zzy.champions.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ChampionAndDetail(
    @Embedded val champion: Champion,
    @Relation(
        parentColumn = "id",
        entityColumn = "championId"
    ) var detail: ChampionDetail
)