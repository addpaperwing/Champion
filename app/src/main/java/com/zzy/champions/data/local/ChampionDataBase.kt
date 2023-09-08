package com.zzy.champions.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zzy.champions.data.model.Champion

@Database(
    entities = [Champion::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChampionDataBase: RoomDatabase() {
    abstract fun championDao(): ChampionDao
}