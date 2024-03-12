package com.zzy.champions.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zzy.champions.data.model.ChampionDetail

@Dao
interface ChampionDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: ChampionDetail)
    @Query("SELECT * FROM ChampionDetail WHERE `championId` IS :id  LIMIT 1")
    suspend fun getDetail(id: String): ChampionDetail?

    @Query("DELETE FROM championdetail")
    suspend fun clearDetailData()
}