package com.zzy.champions.components

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.local.ChampionAndDetailPreviewParameterProvider
import com.zzy.champions.data.local.ChampionDataPreviewParameterProvider
import com.zzy.champions.data.local.db.ChampionDataBase
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.NAME_OF_BUILD_OPGG
import com.zzy.champions.data.model.URL_OF_OPGG
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: ChampionDataBase

    private val championData = ChampionDataPreviewParameterProvider().values.first()
    private val championAndDetail = ChampionAndDetailPreviewParameterProvider().values.first()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, ChampionDataBase::class.java).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun championDao_table_functions()= runTest {
        //Test 'updateChampionDetailData' function
        db.championDao().insertChampionDetail((championAndDetail.detail))

        //Test 'updateChampionDetailData' result
        //Test 'getChampionDetail' function
        val detailBeforeUpdateChampions = db.championDao().getChampionDetail("Akali")
        assertEquals(
            detailBeforeUpdateChampions?.championId,
            championAndDetail.detail.championId
        )


        //Test 'updateChampionBasicData' function
        db.championDao().updateLocalChampionData(championData.champions)

        //Test 'updateChampionBasicData' result1 - insert data list
        //Test 'getAllChampionData' function
        val all = db.championDao().queryChampionsById("")
        assertEquals(all[0], championData.champions[0])
        assertEquals(all[1], championData.champions[1])

        //Test 'updateChampionBasicData' result2 - clear detail table
        val detailAfterUpdateChampions =
            db.championDao().getChampionDetail(championAndDetail.champion.id)
        assertEquals(detailAfterUpdateChampions, null)


        //Test 'searchChampions'
        val searchAResult = db.championDao().queryChampionsById("a")
        assertEquals(searchAResult.size, 6)

        val searchBResult = db.championDao().queryChampionsById("b")
        assertEquals(searchBResult.size, 1)

        val searchAatroxResult = db.championDao().queryChampionsById("Aatrox")
        assertEquals(searchAatroxResult[0], championData.champions[0])
        assertEquals(searchAatroxResult.size, 1)
    }

    @Test
    @Throws(Exception::class)
    fun championBuildDao_functions() = runTest {

        val opgg = ChampionBuild(NAME_OF_BUILD_OPGG, URL_OF_OPGG)
        db.championBuildDao().addNewBuild(opgg)
        assertEquals(db.championBuildDao().getBuilds().first()[0], opgg)

        val newUrl = "url"
        val editedBuild = db.championBuildDao().getBuilds().first()[0].apply { url = newUrl }
        db.championBuildDao().updateBuild(editedBuild)
        assertEquals(db.championBuildDao().getBuilds().first()[0].url, newUrl)

        db.championBuildDao().deleteBuild(db.championBuildDao().getBuilds().first()[0].id)
        assertTrue(db.championBuildDao().getBuilds().first().isEmpty())
    }
}