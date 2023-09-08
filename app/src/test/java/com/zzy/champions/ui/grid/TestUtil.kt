package com.zzy.champions.ui.grid

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Stats

object TestUtil {

    internal val LOCAL_CHAMP_NAME = "local"
    internal val REMOTE_CHAMP_NAME = "remote"

    fun aatrox(remote: Boolean = false): Champion = Champion(
        "Aatrox",
        266,
        if (remote) REMOTE_CHAMP_NAME else LOCAL_CHAMP_NAME,
        "the Darkin Blade",
        emptyList(),
        "",
        Info(),
        Stats()
    )
}