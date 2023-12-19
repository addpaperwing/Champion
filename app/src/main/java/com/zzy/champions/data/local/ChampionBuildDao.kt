package com.zzy.champions.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zzy.champions.data.model.ChampionBuild


@Dao
abstract class ChampionBuildDao {

    @Query("SELECT * FROM ChampionBuild")
    abstract fun getBuilds(): List<ChampionBuild>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addNewBuild(vararg build: ChampionBuild)

    @Insert
    abstract fun addNewBuild(builds: List<ChampionBuild>)

    @Update
    abstract fun updateBuild(build: ChampionBuild)

    @Delete
    abstract fun deleteBuild(build: ChampionBuild)

    @Transaction
    open fun addNewAndRefreshBuilds(build: ChampionBuild): List<ChampionBuild> {
        addNewBuild(build)
        return getBuilds()
    }

    @Transaction
    open fun updateAndRefreshBuilds(build: ChampionBuild): List<ChampionBuild> {
        updateBuild(build = build)
        return getBuilds()
    }

    @Transaction
    open fun deleteAndRefreshBuilds(build: ChampionBuild): List<ChampionBuild> {
        deleteBuild(build)
        return getBuilds()
    }
}