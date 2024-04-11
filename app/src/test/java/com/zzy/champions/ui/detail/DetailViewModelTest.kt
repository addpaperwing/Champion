package com.zzy.champions.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.zzy.champions.data.model.ChampionAndDetail
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.MainDispatcherRule
import com.zzy.champions.ui.TestUtil.createChampion
import com.zzy.champions.ui.TestUtil.createChampionDetail
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @MockK
    private lateinit var repository: DetailRepository

    private val savedStateHandle = SavedStateHandle()

    private lateinit var viewModel: DetailViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = DetailViewModel(savedStateHandle, repository, Dispatchers.Main)
    }

    @Test
    fun getChampionAndDetail_From_LoadingState_To_SuccessState() {
        val id = "aatrox"
        val aatorx = createChampion(id)
        val detail = createChampionDetail(id)
        val championAndDetail = ChampionAndDetail(aatorx, detail)
        coEvery {
            repository.getChampionAndDetail(any())
        } coAnswers {
            delay(200)
            championAndDetail
        }

        runTest {
            viewModel.getChampionAndDetail()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.result.value)

            advanceUntilIdle()
            assertEquals(UiState.Success(championAndDetail), viewModel.result.value)
        }
    }

    @Test
    fun getChampionAndDetail_From_LoadingState_To_ErrorState() {
        val ioException = IOException("error")

        coEvery {
            repository.getChampionAndDetail(any())
        } coAnswers {
            delay(200)
            throw ioException
        }

        runTest {
            viewModel.getChampionAndDetail()

            advanceTimeBy(199)
            assertEquals(UiState.Loading, viewModel.result.value)

            advanceUntilIdle()
            assertEquals(UiState.Error(ioException), viewModel.result.value)
        }
    }

    @Test
    fun updateBannerSplash() {
        val id = "aatrox"
        val detail = ChampionDetail(
            championId = id,
            skins = listOf(
                SkinNumber(0, ""),
                SkinNumber(1, "")
            ),
            lore = "",
            spells = emptyList(),
            passive = Passive(
                name = "", description = "", image = Image(full = "")
            )
        )
        val skinNum = 1

        coJustRun { repository.updateChampionDetailSplash(detail) }

        runTest {
            viewModel.saveBannerSplash(detail, skinNum)

            advanceUntilIdle()

            assertEquals(detail.splashIndex, skinNum)
        }
    }
}