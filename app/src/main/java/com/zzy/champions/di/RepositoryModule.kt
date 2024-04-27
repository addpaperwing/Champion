package com.zzy.champions.di

import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionBuildRepository
import com.zzy.champions.data.repository.ChampionRepository
import com.zzy.champions.data.repository.DefaultAppDataRepository
import com.zzy.champions.data.repository.DefaultChampionBuildRepository
import com.zzy.champions.data.repository.DefaultChampionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface RepositoryModule {

    @Binds
    fun bindAppDataRepository(appDataRepository: DefaultAppDataRepository): AppDataRepository

    @Binds
    fun bindChampionRepository(championRepository: DefaultChampionRepository): ChampionRepository

    @Binds
    fun bindChampionBuildRepository(championBuildRepository: DefaultChampionBuildRepository): ChampionBuildRepository
}