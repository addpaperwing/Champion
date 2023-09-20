package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

//stats: {
//hp: 650,
//hpperlevel: 114,
//mp: 0,
//mpperlevel: 0,
//movespeed: 345,
//armor: 38,
//armorperlevel: 4.45,
//spellblock: 32,
//spellblockperlevel: 2.05,
//attackrange: 175,
//hpregen: 3,
//hpregenperlevel: 1,
//mpregen: 0,
//mpregenperlevel: 0,

//crit: 0,
//critperlevel: 0,
//attackdamage: 60,
//attackdamageperlevel: 5,
//attackspeedperlevel: 2.5,
//attackspeed: 0.651
//},
@JsonClass(generateAdapter = true)
data class Stats(
    val hp: BigDecimal = BigDecimal.ZERO,
    val hpperlevel: BigDecimal = BigDecimal.ZERO,

    val mp: BigDecimal = BigDecimal.ZERO,
    val mpperlevel: BigDecimal = BigDecimal.ZERO,

    val hpregen: BigDecimal = BigDecimal.ZERO,
    val hpregenperlevel: BigDecimal = BigDecimal.ZERO,

    val mpregen: BigDecimal = BigDecimal.ZERO,
    val mpregenperlevel: BigDecimal = BigDecimal.ZERO,

    val armor: BigDecimal = BigDecimal.ZERO,
    val armorperlevel: BigDecimal = BigDecimal.ZERO,

    val spellblock: BigDecimal = BigDecimal.ZERO,
    val spellblockperlevel: BigDecimal = BigDecimal.ZERO,

    val attackdamage: BigDecimal = BigDecimal.ZERO,
    val attackdamageperlevel: BigDecimal = BigDecimal.ZERO,

    val attackspeedperlevel: BigDecimal = BigDecimal.ZERO,
    val attackspeed: BigDecimal = BigDecimal.ZERO,


    val movespeed: BigDecimal = BigDecimal.ZERO,
    val attackrange: BigDecimal = BigDecimal.ZERO,

    val crit: BigDecimal = BigDecimal.ZERO,
    val critperlevel: BigDecimal = BigDecimal.ZERO,
)

//hp,ms,arm,atr,atd,ats