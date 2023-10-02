package com.zzy.champions.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zzy.champions.data.model.ChampionDetail

@Dao
interface ChampionDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(detail: ChampionDetail)
    @Query("SELECT * FROM ChampionDetail WHERE `championId` IS :id  LIMIT 1")
    fun getDetail(id: String): ChampionDetail?
//
//    @Query("UPDATE ChampionDetail SET splashIndex=:index WHERE id = :id")
//    fun updateSplashIndex(id: String, index: Int)
}