package com.zzy.champions.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zzy.champions.data.model.Champion

@Dao
interface ChampionDao {

    @Query("SELECT * FROM champion ORDER BY name ASC")
    fun getAll(): List<Champion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(champions:  List<Champion>)

    @Query("SELECT * FROM champion WHERE `id` IS :id  LIMIT 1")
    fun findById(id: String): Champion
}