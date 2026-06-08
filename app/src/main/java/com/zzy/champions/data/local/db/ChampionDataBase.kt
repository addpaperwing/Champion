package com.zzy.champions.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Item

@Database(
    entities = [Champion::class, ChampionDetail::class, ChampionBuild::class, Item::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChampionDataBase : RoomDatabase() {
    abstract fun championDao(): ChampionDao
    abstract fun championBuildDao(): ChampionBuildDao
    abstract fun itemDao(): ItemDao
}
