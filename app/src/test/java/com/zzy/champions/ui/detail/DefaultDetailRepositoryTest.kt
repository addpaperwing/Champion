package com.zzy.champions.ui.detail

import com.zzy.champions.data.local.ChampionDataBase
import com.zzy.champions.data.local.DataStoreManager
import com.zzy.champions.data.remote.Api
import com.zzy.champions.ui.MainDispatcherRule
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule

class DefaultDetailRepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @MockK
    private lateinit var api: Api
    @MockK
    private lateinit var dsManager: DataStoreManager
    @MockK
    private lateinit var db: ChampionDataBase

    private lateinit var repository: DefaultDetailRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = DefaultDetailRepository(api, dsManager, db)
    }


}