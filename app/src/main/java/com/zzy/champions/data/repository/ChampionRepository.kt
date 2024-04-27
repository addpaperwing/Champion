package com.zzy.champions.data.repository

import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.ChampionResponse

interface ChampionRepository {
    suspend fun getRemoteChampions(version: String, language: String): ChampionResponse
    suspend fun saveLocalChampions(champions: List<Champion>)
    suspend fun searchChampionsBy(id: String): List<Champion>

    suspend fun getRemoteChampionDetail(version: String, language: String, id: String): ChampionDetail?


    suspend fun getLocalChampionDetail(championId: String): ChampionDetail?

    suspend fun saveChampionDetail(championDetail: ChampionDetail)

    suspend fun getChampionAndDetail(id: String): ChampionAndDetail
}