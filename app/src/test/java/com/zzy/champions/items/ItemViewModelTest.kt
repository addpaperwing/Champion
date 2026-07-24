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
import com.zzy.champions.retiredTrinket
import com.zzy.champions.sorceresShoes
import com.zzy.champions.ui.items.CATEGORY_BOOTS
import com.zzy.champions.ui.items.CATEGORY_LEGENDARY
import com.zzy.champions.ui.items.CATEGORY_STARTER
import com.zzy.champions.ui.items.GAME_MODE_ARAM
import com.zzy.champions.ui.items.GAME_MODE_ARENA
import com.zzy.champions.ui.items.GAME_MODE_SUMMONERS_RIFT
import com.zzy.champions.ui.items.ItemGroup
import com.zzy.champions.ui.items.ItemListDisplay
import com.zzy.champions.ui.items.ItemViewModel
import androidx.lifecycle.SavedStateHandle
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private val dataRefreshed = MutableSharedFlow<Unit>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        coEvery { appDataRepository.getLanguage() } returns flowOf(LANGUAGE_US)
        coEvery { appDataRepository.dataRefreshed } returns dataRefreshed
        itemRepository = TestItemRepository()
        useCase = GetItemDataUseCase(itemRepository, appDataRepository, Dispatchers.Main)
        viewModel = ItemViewModel(useCase, appDataRepository, SavedStateHandle())
    }

    private fun groupsNow(): List<Pair<String, List<com.zzy.champions.data.model.Item>>> =
        (viewModel.itemListState.value as UiState.Success).data.groups
            .map { (name, groups) -> name to groups.map { it.primary } }

    @Test
    fun stateIsInitiallyLoading() {
        assertEquals(UiState.Loading, viewModel.itemListState.value)
    }

    @Test
    fun items_loadsSuccessfully() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_BOOTS, CATEGORY_LEGENDARY), groups.map { it.first })
        assertEquals(3, groups.sumOf { it.second.size })
        job.cancel()
    }

    @Test
    fun dataRefreshed_triggersRefetch() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()
        // TestItemRepository starts pre-cached, so the initial load serves local data directly
        // without ever calling the remote fetch.
        assertEquals(0, itemRepository.getRemoteItemsCallCount)

        // Simulates SettingsViewModel.clearAndRefresh(): local cache cleared, then the shared
        // signal fires. This must reach ItemViewModel even though — in the real app — the
        // Items tab's NavBackStackEntry may not currently be live on the back stack.
        itemRepository.clearLocalItems()
        dataRefreshed.emit(Unit)
        advanceUntilIdle()

        assertEquals(1, itemRepository.getRemoteItemsCallCount)
        assertEquals(3, groupsNow().sumOf { it.second.size })
        job.cancel()
    }

    @Test
    fun unavailableItem_excludedFromItemListState() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        val allItems = groupsNow().flatMap { it.second }
        assertTrue(retiredTrinket !in allItems)
        assertEquals(3, allItems.size)
        job.cancel()
    }

    @Test
    fun unavailableItem_excludedFromAvailableTags() = runTest {
        val job = launch { viewModel.availableTags.collect() }
        advanceUntilIdle()

        assertTrue("Trinket" !in viewModel.availableTags.value)
        job.cancel()
    }

    @Test
    fun unavailableItem_getItemByIdReturnsNull() = runTest {
        advanceUntilIdle()

        assertNull(viewModel.getGroupById(retiredTrinket.id))
    }

    @Test
    fun selectedItem_isNullInitially() {
        assertNull(viewModel.selectedItem.value)
    }

    @Test
    fun selectItem_updatesSelectedItem() = runTest {
        val group = ItemGroup(listOf(infinityEdge))
        viewModel.selectItem(group)
        assertEquals(group, viewModel.selectedItem.value)
    }

    @Test
    fun dismissItem_clearsSelectedItem() = runTest {
        viewModel.selectItem(ItemGroup(listOf(infinityEdge)))
        viewModel.dismissItem()
        assertNull(viewModel.selectedItem.value)
    }

    @Test
    fun tagFilter_hidesEmptyCategoriesButKeepsMatchingOnes() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // Only longSword and infinityEdge carry "Damage"; sorceresShoes doesn't, so
        // CATEGORY_BOOTS (its only member) must drop out entirely, header included,
        // while CATEGORY_STARTER and CATEGORY_LEGENDARY keep their (unfiltered) members.
        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()

        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_LEGENDARY), groups.map { it.first })
        assertEquals(listOf(longSword), groups.first { it.first == CATEGORY_STARTER }.second)
        assertEquals(listOf(infinityEdge), groups.first { it.first == CATEGORY_LEGENDARY }.second)
        job.cancel()
    }

    @Test
    fun multipleTagsSelected_matchesOnlyItemWithBothTags() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        viewModel.toggleTagFilter("CriticalStrike")
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_LEGENDARY to listOf(infinityEdge)), groupsNow())
        job.cancel()
    }

    @Test
    fun multipleTagsSelected_excludesItemsMissingOneTag() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // sorceresShoes has "Boots" but not "CriticalStrike"; infinityEdge has "CriticalStrike"
        // but not "Boots". Neither item has both, so AND semantics must exclude both, leaving
        // every group empty.
        viewModel.toggleTagFilter("Boots")
        viewModel.toggleTagFilter("CriticalStrike")
        advanceUntilIdle()

        assertTrue(groupsNow().isEmpty())
        job.cancel()
    }

    @Test
    fun clearFilters_restoresAllCategories() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_BOOTS, CATEGORY_LEGENDARY), groups.map { it.first })
        assertEquals(3, groups.sumOf { it.second.size })
        job.cancel()
    }

    @Test
    fun searchText_narrowsActiveTagFilter() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleTagFilter("Damage")
        viewModel.updateSearchQuery("Infinity")
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_LEGENDARY to listOf(infinityEdge)), groupsNow())
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
        // "raw tag" chips alongside the category headers of the same name.
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
    fun selectedGameModes_isEmptyInitially() {
        assertTrue(viewModel.selectedGameModes.value.isEmpty())
    }

    @Test
    fun gameModeFilter_hidesEmptyCategoriesButKeepsMatchingOnes() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_ARENA)
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_BOOTS to listOf(sorceresShoes)), groupsNow())
        job.cancel()
    }

    @Test
    fun togglingSameGameModeTwice_clearsSelection() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()

        assertTrue(viewModel.selectedGameModes.value.isEmpty())
        val groups = groupsNow()
        assertEquals(listOf(CATEGORY_STARTER, CATEGORY_BOOTS, CATEGORY_LEGENDARY), groups.map { it.first })
        job.cancel()
    }

    @Test
    fun togglingTwoGameModes_bothStaySelectedAndMustBothMatch() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        assertEquals(
            listOf(CATEGORY_STARTER to listOf(longSword), CATEGORY_LEGENDARY to listOf(infinityEdge)),
            groupsNow(),
        )

        // Adding ARAM on top of Summoner's Rift narrows to items available on BOTH —
        // longSword is Summoner's Rift only (maps["12"] == false), so it must drop out,
        // taking CATEGORY_STARTER's header with it.
        viewModel.toggleGameMode(GAME_MODE_ARAM)
        advanceUntilIdle()
        assertEquals(setOf(GAME_MODE_SUMMONERS_RIFT, GAME_MODE_ARAM), viewModel.selectedGameModes.value)
        assertEquals(listOf(CATEGORY_LEGENDARY to listOf(infinityEdge)), groupsNow())
        job.cancel()
    }

    @Test
    fun gameModeAndTagCombined_mustMatchBoth() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        // sorceresShoes is the only item tagged "SpellDamage", but it has no Summoner's Rift
        // availability — AND semantics must exclude it even though the tag alone would match it.
        viewModel.toggleTagFilter("SpellDamage")
        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()

        assertTrue(groupsNow().isEmpty())
        job.cancel()
    }

    @Test
    fun searchText_combinedWithGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_ARAM)
        viewModel.updateSearchQuery("Sorcerer")
        advanceUntilIdle()

        assertEquals(listOf(CATEGORY_BOOTS to listOf(sorceresShoes)), groupsNow())
        job.cancel()
    }

    @Test
    fun tagFilter_matchesGroupWhenTagIsOnlyOnNonPrimaryVariant() = runTest {
        // srVariant (shorter id "3031") is primary and doesn't carry "ArenaOnly"; only the
        // Arena variant does. availableTags surfaces "ArenaOnly" from every raw variant, so
        // selecting it must still match the merged group via a variant-wide tag lookup,
        // not just group.primary.tags.
        val srVariant = infinityEdge.copy(tags = listOf("Damage", "Legendary"))
        val arenaVariant = infinityEdge.copy(
            id = "223031",
            tags = listOf("Damage", "Legendary", "ArenaOnly"),
            maps = mapOf("11" to false, "12" to false, "22" to false, "30" to true),
        )
        val customRepository = TestItemRepository(listOf(srVariant, arenaVariant))
        val customViewModel = ItemViewModel(
            GetItemDataUseCase(customRepository, appDataRepository, Dispatchers.Main),
            appDataRepository,
            SavedStateHandle(),
        )

        val job = launch { customViewModel.itemListState.collect() }
        val tagsJob = launch { customViewModel.availableTags.collect() }
        advanceUntilIdle()
        assertTrue("ArenaOnly" in customViewModel.availableTags.value)

        customViewModel.toggleTagFilter("ArenaOnly")
        advanceUntilIdle()

        val groups = (customViewModel.itemListState.value as UiState.Success).data.groups
        assertEquals(1, groups.sumOf { it.second.size })
        job.cancel()
        tagsJob.cancel()
    }

    @Test
    fun clearFilters_alsoClearsGameMode() = runTest {
        val job = launch { viewModel.itemListState.collect() }
        advanceUntilIdle()

        viewModel.toggleGameMode(GAME_MODE_SUMMONERS_RIFT)
        advanceUntilIdle()
        viewModel.clearFilters()
        advanceUntilIdle()

        assertTrue(viewModel.selectedGameModes.value.isEmpty())
        val groups = groupsNow()
        assertEquals(3, groups.sumOf { it.second.size })
        job.cancel()
    }
}
