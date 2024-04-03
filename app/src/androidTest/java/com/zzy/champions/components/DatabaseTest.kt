package com.zzy.champions.components

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.AndroidTestUtil
import com.zzy.champions.data.local.db.ChampionDataBase
import com.zzy.champions.data.local.db.ChampionDatabaseHelper
import com.zzy.champions.data.model.BUILD_OP_GG
import com.zzy.champions.data.model.BUILD_OP_GG_ARAM
import com.zzy.champions.data.model.BUILD_UGG
import com.zzy.champions.data.model.ChampionBuild
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: ChampionDataBase
    private lateinit var dbHelper: ChampionDatabaseHelper

    private val aatroxId = "aatrox"
    private val aatrox = AndroidTestUtil.createChampion(aatroxId)
    private val aatroxDetail = AndroidTestUtil.createChampionDetail(aatroxId)

    private val ahriId = "ahri"
    private val ahri = AndroidTestUtil.createChampion(ahriId)
//    private val ahriDetail = AndroidTestUtil.createChampionDetail(ahriId)

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, ChampionDataBase::class.java).build()
        dbHelper = ChampionDatabaseHelper(db.championDao(), db.championDetailDao(), db.championBuildDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun champion_and_championDetail_table_functions() {
        runTest {
            //Test 'updateChampionDetailData' function
            dbHelper.updateChampionDetailData(aatroxDetail)

            //Test 'updateChampionDetailData' result
            //Test 'getChampionDetail' function
            val detailBeforeUpdateChampions = dbHelper.getChampionDetail(aatroxId)
            assertThat(detailBeforeUpdateChampions?.championId, equalTo(aatroxDetail.championId))


            //Test 'updateChampionBasicData' function
            dbHelper.updateChampionBasicData(listOf(ahri, aatrox))

            //Test 'updateChampionBasicData' result1 - insert data list
            //Test 'getAllChampionData' function
            val all = dbHelper.getAllChampionData()
            assertThat(all[0], equalTo(aatrox))
            assertThat(all[1], equalTo(ahri))

            //Test 'updateChampionBasicData' result2 - clear detail table
            val detailAfterUpdateChampions = dbHelper.getChampionDetail(aatroxId)
            assertThat(detailAfterUpdateChampions, equalTo(null))


            //Test 'searchChampions'
            val searchAResult = dbHelper.searchChampionsById("a")
            assertThat(searchAResult.size, equalTo(2))

            val searchBResult = dbHelper.searchChampionsById("b")
            assertThat(searchBResult.size, equalTo(0))

            val searchAatroxResult = dbHelper.searchChampionsById("aatrox")
            assertThat(searchAatroxResult[0], equalTo(aatrox))
            assertThat(searchAatroxResult.size, equalTo(1))
        }
    }

    @Test
    @Throws(Exception::class)
    fun championBuilds_table_functions() {
        runTest {
            dbHelper.addPresetBuildData()

            var builds: List<ChampionBuild> = dbHelper.getChampionBuilds()
            assertThat(builds[0], equalTo(BUILD_OP_GG))
            assertThat(builds[1], equalTo(BUILD_UGG))
            assertThat(builds[2], equalTo(BUILD_OP_GG_ARAM))

            val editedBuild = BUILD_OP_GG.copy(url = "url")
            builds = dbHelper.editChampionBuild(editedBuild)
            assertThat(builds[0], equalTo(editedBuild))
            assertThat(builds.size, equalTo(3))

            val newBuild = BUILD_OP_GG.copy(nameOfBuild = "new")
            builds = dbHelper.addChampionBuild(newBuild)
            assertThat(builds.size, equalTo(4))
            assertThat(builds[3], equalTo(newBuild))

            builds = dbHelper.deleteChampionBuild(newBuild)
            assertThat(builds.size, equalTo(3))
        }
    }
}