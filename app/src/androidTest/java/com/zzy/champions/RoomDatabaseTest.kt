package com.zzy.champions

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.ChampionDataBase
import com.zzy.champions.data.model.Champion
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var dao: ChampionDao
    private lateinit var db: ChampionDataBase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, ChampionDataBase::class.java).build()
        dao = db.championDao()
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
        val key1 = 1
        val key2 = 2
        val ahri: Champion = AndroidTestUtil.createChampion("Ahri", key1)
        val aatrox: Champion = AndroidTestUtil.createChampion("Aatrox", key2)
        dao.insertList(listOf(ahri, aatrox))
        val result = dao.getAll()
        assertThat(result[0], equalTo(aatrox))
        assertThat(result[1], equalTo(ahri))
    }
}