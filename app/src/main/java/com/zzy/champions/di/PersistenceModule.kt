package com.zzy.champions.di

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zzy.champions.data.local.AppDataSource
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.local.db.ChampionBuildDao
import com.zzy.champions.data.local.db.ChampionDao
import com.zzy.champions.data.local.db.ChampionDataBase
import com.zzy.champions.data.model.NAME_OF_BUILD_OPGG
import com.zzy.champions.data.model.NAME_OF_BUILD_OPGG_ARAM
import com.zzy.champions.data.model.NAME_OF_BUILD_UGG
import com.zzy.champions.data.model.URL_OF_OPGG
import com.zzy.champions.data.model.URL_OF_OPGG_ARAM
import com.zzy.champions.data.model.URL_OF_UGG
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    private const val DB_NAME = "CHAMPION_DB"

    @Provides
    @Singleton
    fun provideLocalDataSource(@ApplicationContext appContext: Context): AppDataSource {
        return DataStoreManager(appContext)
    }


    @Provides
    @Singleton
    fun provideDatabase(application: Application): ChampionDataBase {
        return Room
            .databaseBuilder(application, ChampionDataBase::class.java, DB_NAME)
            .addCallback(object: RoomDatabase.Callback() {

                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    prepopulateChampionBuild(db)
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun prepopulateChampionBuild(db: SupportSQLiteDatabase) {
        val buildOpgg = contentValuesOf(("nameOfBuild" to NAME_OF_BUILD_OPGG), ("url" to URL_OF_OPGG))
        val buildUgg = contentValuesOf(("nameOfBuild" to NAME_OF_BUILD_UGG), ("url" to URL_OF_UGG))
        val buildOpggAram = contentValuesOf(("nameOfBuild" to NAME_OF_BUILD_OPGG_ARAM), ("url" to URL_OF_OPGG_ARAM))
        try {
            db.insert("ChampionBuild", conflictAlgorithm = SQLiteDatabase.CONFLICT_IGNORE, values = buildOpgg)
            db.insert("ChampionBuild", conflictAlgorithm = SQLiteDatabase.CONFLICT_IGNORE, values = buildUgg)
            db.insert("ChampionBuild", conflictAlgorithm = SQLiteDatabase.CONFLICT_IGNORE, values = buildOpggAram)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    @Provides
    fun provideChampionDao(db: ChampionDataBase) : ChampionDao = db.championDao()

    @Provides
    fun provideChampionBuildDao(db: ChampionDataBase): ChampionBuildDao = db.championBuildDao()


}