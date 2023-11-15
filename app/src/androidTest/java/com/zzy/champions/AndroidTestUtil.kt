package com.zzy.champions

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats

object AndroidTestUtil {

    fun createChampion(name: String = "Aatrox"): Champion = Champion(
        name,
        name,
        "the Darkin Blade",
        Image(""),
        emptyList(),
        "",
        Info(),
        Stats()
    )
}