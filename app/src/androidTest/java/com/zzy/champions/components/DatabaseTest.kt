package com.zzy.champions.components

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.local.ChampionAndDetailPreviewParameterProvider
import com.zzy.champions.data.local.ChampionDataPreviewParameterProvider
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

//    private val aatrox =
//    private val aatroxDetail = AndroidTestUtil.createChampionDetail(AndroidTestUtil.AATROX)
//
//    private val ahri = AndroidTestUtil.createChampion(AndroidTestUtil.AHRI, AndroidTestUtil.AHRI_TITLE)
//    private val ahriDetail = AndroidTestUtil.createChampionDetail(ahriId)
    private val championData = ChampionDataPreviewParameterProvider().values.first()
    private val championAndDetail = ChampionAndDetailPreviewParameterProvider().values.first()

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
            dbHelper.updateChampionDetailData(championAndDetail.detail)

            //Test 'updateChampionDetailData' result
            //Test 'getChampionDetail' function
            val detailBeforeUpdateChampions = dbHelper.getChampionDetail("Akali")
            assertThat(detailBeforeUpdateChampions?.championId, equalTo(championAndDetail.detail.championId))


            //Test 'updateChampionBasicData' function
            dbHelper.updateChampionBasicData(championData.champions)

            //Test 'updateChampionBasicData' result1 - insert data list
            //Test 'getAllChampionData' function
            val all = dbHelper.getAllChampionData()
            assertThat(all[0], equalTo(championData.champions[0]))
            assertThat(all[1], equalTo(championData.champions[1]))

            //Test 'updateChampionBasicData' result2 - clear detail table
            val detailAfterUpdateChampions = dbHelper.getChampionDetail(championAndDetail.champion.id)
            assertThat(detailAfterUpdateChampions, equalTo(null))


            //Test 'searchChampions'
            val searchAResult = dbHelper.searchChampionsById("a")
            assertThat(searchAResult.size, equalTo(6))

            val searchBResult = dbHelper.searchChampionsById("b")
            assertThat(searchBResult.size, equalTo(1))

            val searchAatroxResult = dbHelper.searchChampionsById("Aatrox")
            assertThat(searchAatroxResult[0], equalTo(championData.champions[0]))
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

            val newUrl = "url"
            val editedBuild = builds[0].apply {
                url = newUrl
            }
            builds = dbHelper.editChampionBuild(editedBuild)
            assertThat(builds[0].url, equalTo(newUrl))
            assertThat(builds.size, equalTo(3))

            val newBuild = ChampionBuild("new", "url")
            builds = dbHelper.addChampionBuild(newBuild)
            assertThat(builds.size, equalTo(4))
            assertThat(builds[3], equalTo(newBuild))

            builds = dbHelper.deleteChampionBuild(builds[3].id)
            assertThat(builds.size, equalTo(3))
        }
    }
}