package com.zzy.champions.di

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)

                    prepopulateChampionBuild(db)
                }
            })
            .addMigrations(
                object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // No schema changes — version bumped to align with feature/remove-chroma-skins.
                    }
                },
                object : Migration(2, 3) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Remove duplicate default builds created by the onOpen re-insert bug.
                        db.execSQL(
                            "DELETE FROM ChampionBuild WHERE id NOT IN " +
                            "(SELECT MIN(id) FROM ChampionBuild GROUP BY nameOfBuild)"
                        )
                    }
                }
            )
            .build()
    }

    private fun prepopulateChampionBuild(db: SupportSQLiteDatabase) {
        listOf(
            NAME_OF_BUILD_OPGG to URL_OF_OPGG,
            NAME_OF_BUILD_UGG to URL_OF_UGG,
            NAME_OF_BUILD_OPGG_ARAM to URL_OF_OPGG_ARAM
        ).forEach { (name, url) ->
            try {
                val cursor = db.query(
                    "SELECT COUNT(*) FROM ChampionBuild WHERE nameOfBuild = ?",
                    arrayOf(name)
                )
                val exists = cursor.moveToFirst() && cursor.getInt(0) > 0
                cursor.close()
                if (!exists) {
                    db.insert(
                        "ChampionBuild",
                        SQLiteDatabase.CONFLICT_IGNORE,
                        contentValuesOf("nameOfBuild" to name, "url" to url)
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }


    @Provides
    fun provideChampionDao(db: ChampionDataBase) : ChampionDao = db.championDao()

    @Provides
    fun provideChampionBuildDao(db: ChampionDataBase): ChampionBuildDao = db.championBuildDao()


}