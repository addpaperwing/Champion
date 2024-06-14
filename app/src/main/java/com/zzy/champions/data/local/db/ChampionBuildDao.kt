package com.zzy.champions.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zzy.champions.data.model.ChampionBuild


@Dao
abstract class ChampionBuildDao {

    @Query("SELECT * FROM ChampionBuild")
    abstract suspend fun getBuilds(): List<ChampionBuild>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun addNewBuild(vararg build: ChampionBuild)

    @Insert
    abstract suspend fun addNewBuild(builds: List<ChampionBuild>)

    @Update
    abstract suspend fun updateBuild(build: ChampionBuild)

    @Query("DELETE FROM championbuild WHERE id == :id")
    abstract suspend fun deleteBuild(id: Int)

    @Query("DELETE FROM championbuild")
    abstract suspend fun clearBuilds()

    @Transaction
    open suspend fun addNewAndRefreshBuilds(build: ChampionBuild): List<ChampionBuild> {
        addNewBuild(build)
        return getBuilds()
    }

    @Transaction
    open suspend fun updateAndRefreshBuilds(build: ChampionBuild): List<ChampionBuild> {
        updateBuild(build = build)
        return getBuilds()
    }

    @Transaction
    open suspend fun deleteAndRefreshBuilds(id: Int): List<ChampionBuild> {
        deleteBuild(id)
        return getBuilds()
    }
}