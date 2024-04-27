package com.zzy.champions.data.repository

import com.zzy.champions.data.local.db.ChampionDao
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.ChampionResponse
import com.zzy.champions.data.remote.Api
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultChampionRepository @Inject constructor(
    private val api: Api,
    private val dao: ChampionDao,
    private val dispatcher: CoroutineDispatcher,
): ChampionRepository {

    override suspend fun getRemoteChampions(version: String, language: String): ChampionResponse = withContext(dispatcher) {
        api.getChampions(
            version,
            language
        )
    }

    override suspend fun saveLocalChampions(champions: List<Champion>) = withContext(dispatcher) {
        dao.updateLocalChampionData(champions)
    }

    override suspend fun searchChampionsBy(id: String): List<Champion> = withContext(dispatcher) { dao.queryChampionsById(id) }


    override suspend fun getRemoteChampionDetail(
        version: String,
        language: String,
        id: String
    ): ChampionDetail? {
        return api.getChampionDetail(version, language, id).data[id]
    }


    override suspend fun getLocalChampionDetail(championId: String): ChampionDetail? = withContext(dispatcher) { dao.getChampionDetail(championId) }

    override suspend fun saveChampionDetail(championDetail: ChampionDetail) {
        dao.insertChampionDetail(championDetail)
    }

    override suspend fun getChampionAndDetail(id: String): ChampionAndDetail = withContext(dispatcher) { dao.getChampionAndDetail(id) }
}