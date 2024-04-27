package com.zzy.champions.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetail

@Dao
abstract class ChampionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertChampions(champions:  List<Champion>)

    @Query("SELECT * FROM champion WHERE `id` IS :id  LIMIT 1")
    abstract suspend fun getChampion(id: String): Champion

    @Query("SELECT * FROM champion WHERE id LIKE '%' || :query || '%'")
    abstract suspend fun queryChampionsById(query: String): List<Champion>

    @Upsert(entity = ChampionDetail::class)
    abstract suspend fun insertChampionDetail(detail: ChampionDetail)
    @Query("SELECT * FROM ChampionDetail WHERE `championId` IS :id  LIMIT 1")
    abstract suspend fun getChampionDetail(id: String): ChampionDetail?

    @Transaction
    @Query("SELECT * FROM champion WHERE `id` IS :id  LIMIT 1")
    abstract suspend fun getChampionAndDetail(id: String): ChampionAndDetail

//    @Query("DELETE FROM champion")
//    abstract suspend fun clearChampions()

    @Query("DELETE FROM championdetail")
    abstract suspend fun clearDetailData()

    @Transaction
    open suspend fun updateLocalChampionData(champions:  List<Champion>) {
        insertChampions(champions)
        clearDetailData()
    }
}