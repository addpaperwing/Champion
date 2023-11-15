package com.zzy.champions.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail

@Database(
    entities = [Champion::class, ChampionDetail::class, ChampionBuild::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChampionDataBase: RoomDatabase() {
    abstract fun championDao(): ChampionDao
    abstract fun championDetailDao(): ChampionDetailDao

    abstract fun championBuildDao(): ChampionBuildDao
}