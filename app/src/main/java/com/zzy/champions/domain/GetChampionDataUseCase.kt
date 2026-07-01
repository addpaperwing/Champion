package com.zzy.champions.domain

import com.zzy.champions.data.model.ChampionData
import com.zzy.champions.data.model.VersionInfo
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ChampionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton

internal const val DEFAULT_EARLIEST_VERSION = "3.9.7"

@Singleton
class GetChampionDataUseCase @Inject constructor(
    private val championRepository: ChampionRepository,
    private val appDataRepository: AppDataRepository,
    private val dispatcher: CoroutineDispatcher,
//    private val getLatestVersionUseCase: GetLatestVersionUseCase,
) {

    @Volatile private var cachedVersion: String? = null
    suspend operator fun invoke(query: String): UiState<ChampionData> = withContext(dispatcher) {
        val cv = cachedVersion
        if (cv != null) {
            UiState.Success(
                ChampionData(
                    cv,
                    championRepository.searchChampionsBy(query)
                )
            )
        } else {
            val remoteV = try {
                appDataRepository.getRemoteVersion()[0]
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                //return default earliest version when getting remote version fails (network or HTTP error)
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
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    //Update failed (network or HTTP error) - fall back to local data
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
                val v = appDataRepository.getLocalVersion().first()
                cachedVersion = v
                UiState.Success(
                    ChampionData(
                        v,
                        championRepository.searchChampionsBy(query)
                    )
                )
            }
        }
    }

    fun reset() {
        cachedVersion = null
    }

    @VisibleForTesting
    fun getVersion() = cachedVersion

    @VisibleForTesting
    fun setVersion(version: String) { this.cachedVersion = version }
}