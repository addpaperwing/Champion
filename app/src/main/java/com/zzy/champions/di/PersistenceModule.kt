package com.zzy.champions.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.ChampionDataBase
import com.zzy.champions.data.local.DataStoreManager
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
    fun provideDataStoreManager(@ApplicationContext appContext: Context): DataStoreManager {
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
    fun provideChampionDao(db: ChampionDataBase): ChampionDao {
        return db.championDao()
    }
}