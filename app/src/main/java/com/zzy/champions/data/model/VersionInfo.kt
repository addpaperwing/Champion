package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VersionInfo(var needsUpdate: Boolean, val version: String) {

    companion object {
        fun newVersionInfo(local: String?, remote: String): VersionInfo {
            val localInt = local?.replace(".", "")?.toIntOrNull()?:0
            val remoteInt = remote.replace(".", "").toIntOrNull()?:0
            return VersionInfo(
                localInt < remoteInt,
                if (localInt > remoteInt) local!! else remote //when localInt > remoteInt local would never be null
                )
        }
    }
}