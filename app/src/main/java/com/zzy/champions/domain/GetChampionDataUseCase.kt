package com.zzy.champions.domain

import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.model.VersionInfo
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting
import java.io.IOException
import javax.inject.Inject

internal const val DEFAULT_EARLIEST_VERSION = "3.9.7"

class GetChampionDataUseCase @Inject constructor(
    private val championRepository: ChampionRepository,
    private val appDataRepository: AppDataRepository,
    private val dispatcher: CoroutineDispatcher,
//    private val getLatestVersionUseCase: GetLatestVersionUseCase,
) {

    private var cachedVersion : String? = null
    suspend operator fun invoke(query: String): UiState<ChampionData> = withContext(dispatcher) {
        if (cachedVersion != null) {
            UiState.Success(
                ChampionData(
                    cachedVersion!!,
                    championRepository.searchChampionsBy(query)
                )
            )
        } else {
            val remoteV = try {
                appDataRepository.getRemoteVersion()[0]
            } catch (e: IOException) {
                //return default earliest version when get remote version failed
                e.printStackTrace()
                DEFAULT_EARLIEST_VERSION
            }

            val localV = appDataRepository.getLocalVersion().first()

            val versionInfo = VersionInfo.newVersionInfo(localV, remoteV)

            val language = appDataRepository.getLanguage().first()

            if (versionInfo.needsUpdate) {
                try {
                    val champions =
                        championRepository.getRemoteChampions(
                            versionInfo.version,
                            language
                        ).data.map { entry ->
                            entry.value
                        }

                    championRepository.saveLocalChampions(champions)
                    appDataRepository.setLocalVersion(versionInfo.version)


                    //Update successful
                    cachedVersion = versionInfo.version
                    UiState.Success(
                        ChampionData(
                            versionInfo.version,
                            championRepository.searchChampionsBy(query)
                        )
                    )
                } catch (e: IOException) {
                    //Update failed
                    e.printStackTrace()

                    UiState.Success(
                        ChampionData(
                            appDataRepository.getLocalVersion().first(),
                            championRepository.searchChampionsBy(query)
                        )
                    )
                }
            } else {
                //No needs to update
                cachedVersion = appDataRepository.getLocalVersion().first()
                UiState.Success(
                    ChampionData(
                        cachedVersion!!,
                        championRepository.searchChampionsBy(query)
                    )
                )
            }
        }
    }

//    suspend operator fun invoke(query: String) = if (cachedVersion != null) {
//        flowOf(UiState.Success(ChampionData(cachedVersion!!, championRepository.searchChampionsBy(query))))
//    } else {
//        flowOf(
//            try {
//                appDataRepository.getRemoteVersion()[0]
//            } catch (e: IOException) {
//                //return default earliest version when get remote version failed
//                e.printStackTrace()
//                DEFAULT_EARLIEST_VERSION
//            }
//        ).map { remote ->
//            VersionInfo.newVersionInfo(appDataRepository.getLocalVersion().first(), remote)
//        }.zip(appDataRepository.getLanguage()) { versionInfo, language ->
//            if (versionInfo.needsUpdate) {
//                try {
//                    val champions =
//                        championRepository.getRemoteChampions(
//                            versionInfo.version,
//                            language
//                        ).data.map { entry ->
//                            entry.value
//                        }
//
//                    championRepository.saveLocalChampions(champions)
//                    appDataRepository.setLocalVersion(versionInfo.version)
//
//
//                    //Update successful
//                    cachedVersion = versionInfo.version
//                    UiState.Success(
//                        ChampionData(
//                            versionInfo.version,
//                            championRepository.searchChampionsBy(query)
//                        )
//                    )
//                } catch (e: IOException) {
//                    //Update failed
//                    e.printStackTrace()
//
//                    UiState.Success(
//                        ChampionData(
//                            appDataRepository.getLocalVersion().first(),
//                            championRepository.searchChampionsBy(query)
//                        )
//                    )
//                }
//            } else {
//                //No needs to update
//                cachedVersion = appDataRepository.getLocalVersion().first()
//                UiState.Success(
//                    ChampionData(
//                        cachedVersion!!,
//                        championRepository.searchChampionsBy(query)
//                    )
//                )
//            }
//        }
//    }.flowOn(dispatcher)

    @VisibleForTesting
    fun getVersion() = cachedVersion

    @VisibleForTesting
    fun setVersion(version: String) { this.cachedVersion = version }
}