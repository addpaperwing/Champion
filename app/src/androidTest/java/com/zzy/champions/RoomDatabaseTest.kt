package com.zzy.champions

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.local.db.ChampionDataBase
import com.zzy.champions.data.local.db.ChampionDatabaseHelper
import com.zzy.champions.data.model.Champion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var db: ChampionDataBase
    private lateinit var dbHelper: ChampionDatabaseHelper

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, ChampionDataBase::class.java).build()
        dbHelper = ChampionDatabaseHelper(db.championDao(), db.championDetailDao(), db.championBuildDao())

        runBlocking {
            
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

//    @Test
//    @Throws(Exception::class)
//    fun writeChampionsAndReadByFindKey() {
//        val key = 1
//        val champ: Champion = AndroidTestUtil.createChampion(key = key)
//        dao.insertList(listOf(champ))
//        val result = dao.findById(key)
//        assertThat(result, equalTo(champ))
//    }

    @Test
    @Throws(Exception::class)
    fun writeChampionsAndReadList_OrderedByNameASC() {
        val ahri: Champion = AndroidTestUtil.createChampion("Ahri")
        val aatrox: Champion = AndroidTestUtil.createChampion("Aatrox")

         runTest {
             dbHelper.updateChampionBasicData(listOf(ahri, aatrox))
            val result = dbHelper.getAllChampionData()

             assertThat(result[0], equalTo(aatrox))
             assertThat(result[1], equalTo(ahri))
        }
    }
}