package com.zzy.champions.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.local.LocalDataSource
import com.zzy.champions.data.local.db.ChampionDataBase
import com.zzy.champions.data.local.db.ChampionDatabaseHelper
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
    fun provideLocalDataSource(@ApplicationContext appContext: Context): LocalDataSource {
        return DataStoreManager(appContext)
    }


    @Provides
    @Singleton
    fun provideDatabase(application: Application): ChampionDataBase {
        return Room
            .databaseBuilder(application, ChampionDataBase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideChampionDatabaseHelper(db: ChampionDataBase): ChampionDatabaseHelper {
        return ChampionDatabaseHelper(
            db.championDao(),
            db.championDetailDao(),
            db.championBuildDao()
        )
    }
}