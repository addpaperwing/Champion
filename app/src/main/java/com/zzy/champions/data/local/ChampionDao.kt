package com.zzy.champions.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail

@Dao
interface ChampionDao {

    @Query("SELECT * FROM champion ORDER BY name ASC")
    fun getAll(): List<Champion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(champions:  List<Champion>)

    @Query("SELECT * FROM champion WHERE `id` IS :id  LIMIT 1")
    fun findById(id: String): Champion

    @Query("SELECT * FROM champion WHERE name LIKE :query || '%'")
    fun findChampion(query: String): List<Champion>

    @Transaction
    @Query("SELECT * FROM champion WHERE `id` IS :id  LIMIT 1")
    fun getChampionAndDetail(id: String): ChampionAndDetail
}