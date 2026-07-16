package com.zzy.champions.domain

import com.zzy.champions.data.local.PENDING_VERSION
import com.zzy.champions.data.model.VersionInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionInfoTest {

    @Test
    fun localOlderThanRemote_sameSegmentLength_needsUpdateAndUsesRemote() {
        val info = VersionInfo.newVersionInfo(local = "14.0.0", remote = "14.1.0")

        assertTrue(info.needsUpdate)
        assertEquals("14.1.0", info.version)
    }

    @Test
    fun localNewerThanRemote_doesNotNeedUpdateAndUsesLocal() {
        val info = VersionInfo.newVersionInfo(local = "14.0.0", remote = "3.9.7")

        assertFalse(info.needsUpdate)
        assertEquals("14.0.0", info.version)
    }

    @Test
    fun equalVersions_doesNotNeedUpdate() {
        val info = VersionInfo.newVersionInfo(local = "14.1.0", remote = "14.1.0")

        assertFalse(info.needsUpdate)
        assertEquals("14.1.0", info.version)
    }

    @Test
    fun nullLocal_needsUpdateAndUsesRemote() {
        val info = VersionInfo.newVersionInfo(local = null, remote = "14.1.0")

        assertTrue(info.needsUpdate)
        assertEquals("14.1.0", info.version)
    }

    @Test
    fun defaultLocalZero_needsUpdateAndUsesRemote() {
        val info = VersionInfo.newVersionInfo(local = PENDING_VERSION, remote = "4.0.0")

        assertTrue(info.needsUpdate)
        assertEquals("4.0.0", info.version)
    }

    // Regression: stripping dots and parsing one Int mis-orders versions whose
    // segments have different digit counts. "14.1.1" -> 1411 vs "9.24.1" -> 9241.
    @Test
    fun localNewerMajor_butRemoteHasMoreDigitsInMinor_doesNotNeedUpdate() {
        val info = VersionInfo.newVersionInfo(local = "14.1.1", remote = "9.24.1")

        assertFalse(info.needsUpdate)
        assertEquals("14.1.1", info.version)
    }

    @Test
    fun remoteNewerMajor_butLocalHasMoreDigitsInMinor_needsUpdate() {
        val info = VersionInfo.newVersionInfo(local = "9.24.1", remote = "14.1.1")

        assertTrue(info.needsUpdate)
        assertEquals("14.1.1", info.version)
    }

    // Regression: "1.1.1" -> 111 vs "1.0.20" -> 1020 wrongly ordered local as older.
    @Test
    fun localNewerInMinor_comparedSegmentwise_doesNotNeedUpdate() {
        val info = VersionInfo.newVersionInfo(local = "1.1.1", remote = "1.0.20")

        assertFalse(info.needsUpdate)
        assertEquals("1.1.1", info.version)
    }
}
