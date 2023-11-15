package com.zzy.champions.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zzy.champions.data.model.ChampionBuild


@Dao
interface ChampionBuildDao {

    @Query("SELECT * FROM ChampionBuild")
    fun getBuilds(): List<ChampionBuild>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNewBuild(build: ChampionBuild)

    @Delete
    fun deleteBuild(build: ChampionBuild)
}