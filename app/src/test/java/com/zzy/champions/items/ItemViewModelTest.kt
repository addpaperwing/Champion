package com.zzy.champions.items

import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestItemRepository
import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.domain.GetItemDataUseCase
import com.zzy.champions.infinityEdge
import com.zzy.champions.longSword
import com.zzy.champions.sorceresShoes
import com.zzy.champions.ui.items.CATEGORY_BOOTS
import com.zzy.champions.ui.items.CATEGORY_LEGENDARY
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
import com.zzy.champions.ui.items.ItemListDisplay
import com.zzy.champions.ui.items.ItemViewModel
import androidx.lifecycle.SavedStateHandle
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @MockK private lateinit var appDataRepository: AppDataRepository
    private lateinit var itemRepository: TestItemRepository
    private lateinit var useCase: GetItemDataUseCase
    private lateinit var viewModel: ItemViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        coEvery { appDataRepository.getLanguage() } returns flowOf(LANGUAGE_US)
        itemRepository = TestItemRepository()
        useCase = GetItemDataUseCase(itemRepository, appDataRepository, Dispatchers.Main)
        viewModel = ItemViewModel(useCase, appDataRepository, SavedStateHandle())
    }

    @Test
    fun stateIsInitiallyLoading() {
        assertEquals(UiState.Loading, viewModel.itemListState.value)
    }

    @Test
    fun items_loadsSuccessfully() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        val state = viewModel.itemListState.value
        assertTrue(state is UiState.Success)
        val display = (state as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        val totalItems = (display as ItemListDisplay.Categorized).groups.sumOf { it.second.size }
        assertEquals(3, totalItems)
        job.cancel()
    }

    @Test
    fun selectedItem_isNullInitially() {
        assertNull(viewModel.selectedItem.value)
    }

    @Test
    fun selectItem_updatesSelectedItem() = runTest {
        viewModel.selectItem(infinityEdge)
        assertEquals(infinityEdge, viewModel.selectedItem.value)
    }

    @Test
    fun dismissItem_clearsSelectedItem() = runTest {
        viewModel.selectItem(infinityEdge)
        viewModel.dismissItem()
        assertNull(viewModel.selectedItem.value)
    }

    @Test
    fun categoryFilter_showsFlatDisplayWithMatchingItems() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleCategoryFilter(CATEGORY_BOOTS)
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun tagFilter_showsFlatDisplayWithMatchingItems() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(longSword, infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun multipleTagsSelected_matchEitherTag() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Boots")
        viewModel.toggleTagFilter("CriticalStrike")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes, infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun categoryAndTagCombined_mustMatchBoth() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleCategoryFilter(CATEGORY_LEGENDARY)
        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun clearFilters_returnsToCategorizedDisplay() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleCategoryFilter(CATEGORY_BOOTS)
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        job.cancel()
    }

    @Test
    fun searchText_narrowsActiveTagFilter() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        viewModel.updateSearchQuery("Infinity")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun availableTags_derivedFromLoadedItems() = runTest {
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        assertEquals(
            listOf("CriticalStrike", "Damage", "SpellDamage"),
            viewModel.availableTags.value,
        )
        job.cancel()
    }

    @Test
    fun availableTags_excludesTagsMatchingCategoryNames() = runTest {
        // sorceresShoes carries the raw tag "Boots" and infinityEdge carries "Legendary" —
        // both are also fixed category names, so they should not appear as separate
        // "raw tag" chips alongside the "Category: Boots" / "Category: Legendary" chips.
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        val tags = viewModel.availableTags.value
        assertTrue("Boots" !in tags)
        assertTrue("Legendary" !in tags)
        assertTrue("Damage" in tags)
        assertTrue("CriticalStrike" in tags)
        assertTrue("SpellDamage" in tags)
        job.cancel()
    }

    @Test
    fun selectedGameMode_isNullInitially() {
        assertNull(viewModel.selectedGameMode.value)
    }

    @Test
    fun gameModeFilter_showsFlatDisplayWithMatchingItems() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_ARENA)
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun selectingSameGameModeAgain_clearsSelection() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()

        assertNull(viewModel.selectedGameMode.value)
        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        job.cancel()
    }

    @Test
    fun selectingDifferentGameMode_replacesSelection() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        var display = (viewModel.itemListState.value as UiState.Success).data
        assertEquals(listOf(longSword, infinityEdge), (display as ItemListDisplay.Flat).items)

        viewModel.selectGameMode(GAME_MODE_ARAM)
        advanceUntilIdle()
        display = (viewModel.itemListState.value as UiState.Success).data
        assertEquals(listOf(sorceresShoes, infinityEdge), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun gameModeAndCategoryCombined_mustMatchBoth() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // infinityEdge is the only CATEGORY_LEGENDARY item, but it has no Arena availability —
        // AND semantics must exclude it even though the category alone would match it.
        viewModel.toggleCategoryFilter(CATEGORY_LEGENDARY)
        viewModel.selectGameMode(GAME_MODE_ARENA)
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertTrue((display as ItemListDisplay.Flat).items.isEmpty())
        job.cancel()
    }

    @Test
    fun searchText_combinedWithGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_ARAM)
        viewModel.updateSearchQuery("Sorcerer")
        advanceUntilIdle()

        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Flat)
        assertEquals(listOf(sorceresShoes), (display as ItemListDisplay.Flat).items)
        job.cancel()
    }

    @Test
    fun clearFilters_alsoClearsGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.selectGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        assertNull(viewModel.selectedGameMode.value)
        val display = (viewModel.itemListState.value as UiState.Success).data
        assertTrue(display is ItemListDisplay.Categorized)
        job.cancel()
    }
}
