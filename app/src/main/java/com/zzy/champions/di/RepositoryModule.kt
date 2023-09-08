package com.zzy.champions.di

import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.grid.ChampionsRepository
import com.zzy.champions.ui.grid.DefaultChampionRepository
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
    fun provideMainRepository(api: Api, dsManager: DataStoreManager, dao: ChampionDao): ChampionsRepository {
        return DefaultChampionRepository(api, dsManager, dao)
    }

}