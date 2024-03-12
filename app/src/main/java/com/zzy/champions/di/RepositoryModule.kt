package com.zzy.champions.di

import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.local.db.ChampionDatabaseHelper
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.detail.DefaultDetailRepository
import com.zzy.champions.ui.detail.DetailRepository
import com.zzy.champions.ui.index.ChampionRepository
import com.zzy.champions.ui.index.DefaultChampionRepository
import com.zzy.champions.ui.settings.DefaultSettingsRepository
import com.zzy.champions.ui.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.*

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @ViewModelScoped
    @Provides
    fun provideRepository(api: Api, dsManager: DataStoreManager, dbHelper: ChampionDatabaseHelper): ChampionRepository {
        return DefaultChampionRepository(api, dsManager, dbHelper)
    }

    @ViewModelScoped
    @Provides
    fun provideDetailRepository(api: Api, dsManager: DataStoreManager,  dbHelper: ChampionDatabaseHelper): DetailRepository {
        return DefaultDetailRepository(api, dsManager, dbHelper)
    }

    @ViewModelScoped
    @Provides
    fun provideSettingsRepository(api: Api, dsManager: DataStoreManager): SettingsRepository {
        return DefaultSettingsRepository(api, dsManager)
    }
}