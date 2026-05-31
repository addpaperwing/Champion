package com.zzy.champions.data.model

// Computed in-memory only (never serialized), so no Moshi adapter is generated.
class VersionInfo(val needsUpdate: Boolean, val version: String) {

    companion object {
        fun newVersionInfo(local: String?, remote: String): VersionInfo {
            val comparison = compareVersions(local, remote)
            return VersionInfo(
                comparison < 0,
                if (comparison > 0) local!! else remote //when local is newer it is never null
                )
        }

        // Compare dot-separated versions segment by segment, so "14.1.1" > "9.24.1".
        // A null/missing segment is treated as 0. Returns <0, 0, or >0.
        private fun compareVersions(local: String?, remote: String): Int {
            val localParts = local.toVersionParts()
            val remoteParts = remote.toVersionParts()
            for (i in 0 until maxOf(localParts.size, remoteParts.size)) {
                val diff = localParts.getOrElse(i) { 0 }.compareTo(remoteParts.getOrElse(i) { 0 })
                if (diff != 0) return diff
            }
            return 0
        }

        private fun String?.toVersionParts(): List<Int> =
            this?.split(".")?.map { it.toIntOrNull() ?: 0 } ?: emptyList()
    }
}