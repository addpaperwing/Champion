package com.zzy.champions.di

import com.zzy.champions.data.local.ChampionDao
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.detail.ChampionDetailRepository
import com.zzy.champions.ui.detail.DefaultChampionDetailRepository
import com.zzy.champions.ui.index.ChampionIndexRepository
import com.zzy.champions.ui.index.DefaultChampionIndexRepository
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
    fun provideIndexRepository(api: Api, dsManager: DataStoreManager, dao: ChampionDao): ChampionIndexRepository {
        return DefaultChampionIndexRepository(api, dsManager, dao)
    }

    @ViewModelScoped
    @Provides
    fun provideDetailRepository(api: Api, dsManager: DataStoreManager, dao: ChampionDao): ChampionDetailRepository {
        return DefaultChampionDetailRepository(api, dsManager, dao)
    }

}