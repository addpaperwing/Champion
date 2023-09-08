package com.zzy.champions

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats

object AndroidTestUtil {

    fun createChampion(name: String = "Aatrox", key: Int): Champion = Champion(
        "",
        key,
        name,
        "",
        emptyList(),
        "",
        Info(),
        Stats()
    )
}