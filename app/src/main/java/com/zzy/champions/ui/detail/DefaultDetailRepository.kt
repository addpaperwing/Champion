package com.zzy.champions.ui.detail

import com.zzy.champions.data.local.ChampionDataBase
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.remote.Api
import java.io.IOException
import javax.inject.Inject

class DefaultDetailRepository @Inject constructor(
    private val api: Api,
    private val dsManager: DataStoreManager,
    private val db: ChampionDataBase
): DetailRepository {

    override suspend fun getChampionAndDetail(id: String): ChampionAndDetail {
        db.championDetailDao().getDetail(id)?: kotlin.run {
            val detail = api.getChampionDetail(dsManager.getVersion(), dsManager.getLanguage(), id).data[id]?: throw IOException("Champion not found")
            db.championDetailDao().insert(detail = detail)
        }
        return db.championDao().getChampionAndDetail(id)
    }

    override suspend fun updateChampionDetailSplash(detail: ChampionDetail) {
        db.championDetailDao().insert(detail)
    }
}