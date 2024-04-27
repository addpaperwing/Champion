package com.zzy.champions.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zzy.champions.data.model.ChampionBuild
import kotlinx.coroutines.flow.Flow


@Dao
abstract class ChampionBuildDao {

    @Query("SELECT * FROM ChampionBuild")
    abstract fun getBuilds(): Flow<List<ChampionBuild>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun addNewBuild(vararg build: ChampionBuild)

    @Update
    abstract suspend fun updateBuild(build: ChampionBuild)

    @Query("DELETE FROM championbuild WHERE id == :id")
    abstract suspend fun deleteBuild(id: Int)

    @Query("DELETE FROM championbuild")
    abstract suspend fun clearBuilds()
}