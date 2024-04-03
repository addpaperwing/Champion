package com.zzy.champions.data.local.db

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
    suspend fun getAll(): List<Champion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(champions:  List<Champion>)

    @Query("SELECT * FROM champion WHERE `id` IS :id  LIMIT 1")
    suspend fun getChampion(id: String): Champion

    @Query("SELECT * FROM champion WHERE id LIKE '%' || :query || '%'")
    suspend fun queryChampionsById(query: String): List<Champion>

    @Transaction
    @Query("SELECT * FROM champion WHERE `id` IS :id  LIMIT 1")
    suspend fun getChampionAndDetail(id: String): ChampionAndDetail

    @Query("DELETE FROM champion")
    suspend fun clearChampions()
}