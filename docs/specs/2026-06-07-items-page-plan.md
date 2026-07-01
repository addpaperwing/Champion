# Items Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a standalone League of Legends items browser page with category sections, item detail bottom sheet, and a bottom navigation bar alongside the existing champion index.

**Architecture:** Follows the existing Clean Architecture layered pattern (Remote → Repository → UseCase → ViewModel → Compose UI). New `Item` entity cached in Room (DB v3→v4 migration). Bottom nav added to the existing `ChampionApp.kt` Scaffold.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Retrofit + Moshi, Hilt, Coroutines/Flow, Coil (image loading), MockK + JUnit 4 (tests)

**Spec:** `docs/specs/2026-06-07-items-page-design.md`

---

## File Map

### New files
| File | Responsibility |
|---|---|
| `data/model/Item.kt` | Room entity + Moshi model for a single LoL item |
| `data/model/ItemGold.kt` | Nested gold object from Data Dragon API |
| `data/model/ItemResponse.kt` | Moshi wrapper for `item.json` API response |
| `data/local/db/ItemDao.kt` | Room DAO for items table |
| `data/repository/ItemRepository.kt` | Interface: remote fetch + local CRUD |
| `data/repository/DefaultItemRepository.kt` | Implementation |
| `domain/GetItemDataUseCase.kt` | Fetch-if-empty orchestration, returns UiState |
| `ui/items/ItemViewModel.kt` | Exposes items state + selected item for bottom sheet |
| `ui/items/compose/ItemScreen.kt` | Category-section list screen |
| `ui/items/compose/ItemCard.kt` | Single item card (icon + name + gold) |
| `ui/items/compose/ItemBottomSheet.kt` | Detail sheet: stats, description, build path |
| `ui/navigation/ItemsNavigation.kt` | NavGraphBuilder extension + route constant |
| `app/src/test/java/com/zzy/champions/TestItemRepository.kt` | Fake repository for tests |

### Modified files
| File | Change |
|---|---|
| `data/remote/Api.kt` | Add `getItems()` endpoint |
| `data/local/db/Converters.kt` | Add converters for `ItemGold`, `Map<String,Boolean>`, `Map<String,Double>` |
| `data/local/db/ChampionDataBase.kt` | Bump version 3→4, add `Item::class`, add `itemDao()` |
| `di/PersistenceModule.kt` | Add migration 3→4, provide `ItemDao` |
| `di/RepositoryModule.kt` | Bind `ItemRepository → DefaultItemRepository` |
| `ui/navigation/ChampionNavHost.kt` | Add `itemsScreen()` to NavHost graph |
| `ui/ChampionApp.kt` | Add `NavigationBar` to Scaffold; show only on top-level routes |

---

## Task 1: Item Data Models

**Files:**
- Create: `app/src/main/java/com/zzy/champions/data/model/ItemGold.kt`
- Create: `app/src/main/java/com/zzy/champions/data/model/Item.kt`
- Create: `app/src/main/java/com/zzy/champions/data/model/ItemResponse.kt`
- Test: `app/src/test/java/com/zzy/champions/data/model/ItemTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// app/src/test/java/com/zzy/champions/data/model/ItemTest.kt
package com.zzy.champions.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemTest {

    private val testItem = Item(
        id = "3153",
        name = "Blade of the Ruined King",
        description = "<mainText>Deals damage</mainText>",
        plaintext = "Deals bonus damage to high-health enemies",
        image = Image("3153.png"),
        gold = ItemGold(base = 625, purchasable = true, total = 3300, sell = 2310),
        tags = listOf("Damage", "SpellDamage"),
        maps = mapOf("11" to true, "12" to true),
        stats = mapOf("FlatPhysicalDamageMod" to 40.0),
        components = listOf("1043", "3134"),
        upgrades = emptyList(),
    )

    @Test
    fun getIconUrl_returnsCorrectDDragonUrl() {
        assertEquals(
            "https://ddragon.leagueoflegends.com/cdn/14.1.1/img/item/3153.png",
            testItem.getIconUrl("14.1.1")
        )
    }

    @Test
    fun item_withPurchasableTrue_isPurchasable() {
        assertTrue(testItem.gold.purchasable)
    }

    @Test
    fun item_withPurchasableFalse_isNotPurchasable() {
        assertFalse(testItem.copy(gold = testItem.gold.copy(purchasable = false)).gold.purchasable)
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.data.model.ItemTest"
```
Expected: FAIL — `Item`, `ItemGold`, `Image` classes not found.

- [ ] **Step 3: Create ItemGold.kt**

```kotlin
// app/src/main/java/com/zzy/champions/data/model/ItemGold.kt
package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemGold(
    val base: Int = 0,
    val purchasable: Boolean = true,
    val total: Int = 0,
    val sell: Int = 0,
)
```

- [ ] **Step 4: Create Item.kt**

```kotlin
// app/src/main/java/com/zzy/champions/data/model/Item.kt
package com.zzy.champions.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "items")
data class Item(
    @PrimaryKey
    val id: String = "",        // set from map key in repository; empty default lets Moshi skip it
    val name: String = "",
    val description: String = "",
    val plaintext: String = "",
    val image: Image = Image(""),
    val gold: ItemGold = ItemGold(),
    val tags: List<String> = emptyList(),
    val maps: Map<String, Boolean> = emptyMap(),
    val stats: Map<String, Double> = emptyMap(),
    @Json(name = "from") val components: List<String> = emptyList(),  // "from" is SQL reserved word
    @Json(name = "into") val upgrades: List<String> = emptyList(),    // renamed for clarity
) {
    fun getIconUrl(version: String) =
        "https://ddragon.leagueoflegends.com/cdn/$version/img/item/${image.full}"
}
```

- [ ] **Step 5: Create ItemResponse.kt**

```kotlin
// app/src/main/java/com/zzy/champions/data/model/ItemResponse.kt
package com.zzy.champions.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemResponse(val data: Map<String, Item>)
```

- [ ] **Step 6: Run tests to confirm they pass**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.data.model.ItemTest"
```
Expected: PASS (3 tests)

- [ ] **Step 7: Commit**

```
git add app/src/main/java/com/zzy/champions/data/model/ItemGold.kt
git add app/src/main/java/com/zzy/champions/data/model/Item.kt
git add app/src/main/java/com/zzy/champions/data/model/ItemResponse.kt
git add app/src/test/java/com/zzy/champions/data/model/ItemTest.kt
git commit -m "feat: add Item, ItemGold, ItemResponse data models"
```

---

## Task 2: TypeConverters for Item Types

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/data/local/db/Converters.kt`
- Test: `app/src/test/java/com/zzy/champions/data/local/db/ConvertersItemTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// app/src/test/java/com/zzy/champions/data/local/db/ConvertersItemTest.kt
package com.zzy.champions.data.local.db

import com.zzy.champions.data.model.ItemGold
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersItemTest {

    @Test
    fun itemGold_roundTrip() {
        val gold = ItemGold(base = 625, purchasable = true, total = 3300, sell = 2310)
        val json = Converters.fromItemGold(gold)
        assertEquals(gold, Converters.toItemGold(json))
    }

    @Test
    fun stringBooleanMap_roundTrip() {
        val map = mapOf("11" to true, "12" to false, "22" to true)
        val json = Converters.fromStringBooleanMap(map)
        assertEquals(map, Converters.toStringBooleanMap(json))
    }

    @Test
    fun stringDoubleMap_roundTrip() {
        val map = mapOf("FlatPhysicalDamageMod" to 40.0, "PercentLifeStealMod" to 0.15)
        val json = Converters.fromStringDoubleMap(map)
        assertEquals(map, Converters.toStringDoubleMap(json))
    }

    @Test
    fun emptyStringBooleanMap_roundTrip() {
        val map = emptyMap<String, Boolean>()
        val json = Converters.fromStringBooleanMap(map)
        assertEquals(map, Converters.toStringBooleanMap(json))
    }

    @Test
    fun emptyStringDoubleMap_roundTrip() {
        val map = emptyMap<String, Double>()
        val json = Converters.fromStringDoubleMap(map)
        assertEquals(map, Converters.toStringDoubleMap(json))
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.data.local.db.ConvertersItemTest"
```
Expected: FAIL — converter methods not found.

- [ ] **Step 3: Add converters to Converters.kt**

Add the following adapters and converter methods to the existing `Converters.kt` object, after the existing `passiveAdapter` declaration:

```kotlin
// In the private adapter declarations section — add these after passiveAdapter:
@OptIn(ExperimentalStdlibApi::class)
private val itemGoldAdapter: JsonAdapter<ItemGold> = moshi.adapter()

private val stringBooleanMapAdapter: JsonAdapter<Map<String, Boolean>> =
    moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Boolean::class.java))

private val stringDoubleMapAdapter: JsonAdapter<Map<String, Double>> =
    moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Double::class.java))
```

Add these converter methods at the end of the `Converters` object (before the closing `}`):

```kotlin
@TypeConverter
@JvmStatic
fun toItemGold(value: String): ItemGold = itemGoldAdapter.fromJson(value) ?: ItemGold()

@TypeConverter
@JvmStatic
fun fromItemGold(gold: ItemGold): String = itemGoldAdapter.toJson(gold)

@TypeConverter
@JvmStatic
fun toStringBooleanMap(value: String): Map<String, Boolean> =
    stringBooleanMapAdapter.fromJson(value) ?: emptyMap()

@TypeConverter
@JvmStatic
fun fromStringBooleanMap(map: Map<String, Boolean>): String =
    stringBooleanMapAdapter.toJson(map)

@TypeConverter
@JvmStatic
fun toStringDoubleMap(value: String): Map<String, Double> =
    stringDoubleMapAdapter.fromJson(value) ?: emptyMap()

@TypeConverter
@JvmStatic
fun fromStringDoubleMap(map: Map<String, Double>): String =
    stringDoubleMapAdapter.toJson(map)
```

Also add `ItemGold` to the import section at the top of Converters.kt:
```kotlin
import com.zzy.champions.data.model.ItemGold
```

- [ ] **Step 4: Run tests to confirm they pass**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.data.local.db.ConvertersItemTest"
```
Expected: PASS (5 tests)

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/zzy/champions/data/local/db/Converters.kt
git add app/src/test/java/com/zzy/champions/data/local/db/ConvertersItemTest.kt
git commit -m "feat: add Room TypeConverters for ItemGold, Map<String,Boolean>, Map<String,Double>"
```

---

## Task 3: ItemDao + DB Migration v3→v4

**Files:**
- Create: `app/src/main/java/com/zzy/champions/data/local/db/ItemDao.kt`
- Modify: `app/src/main/java/com/zzy/champions/data/local/db/ChampionDataBase.kt`
- Modify: `app/src/main/java/com/zzy/champions/di/PersistenceModule.kt`

- [ ] **Step 1: Create ItemDao.kt**

```kotlin
// app/src/main/java/com/zzy/champions/data/local/db/ItemDao.kt
package com.zzy.champions.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zzy.champions.data.model.Item

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int

    @Query("DELETE FROM items")
    suspend fun clearItems()

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): Item?
}
```

- [ ] **Step 2: Update ChampionDataBase.kt — bump version, add Item entity and itemDao()**

Replace the existing `ChampionDataBase.kt` content:

```kotlin
// app/src/main/java/com/zzy/champions/data/local/db/ChampionDataBase.kt
package com.zzy.champions.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zzy.champions.data.model.Champion
import com.zzy.champions.data.model.ChampionBuild
import com.zzy.champions.data.model.ChampionDetail
import com.zzy.champions.data.model.Item

@Database(
    entities = [Champion::class, ChampionDetail::class, ChampionBuild::class, Item::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChampionDataBase : RoomDatabase() {
    abstract fun championDao(): ChampionDao
    abstract fun championBuildDao(): ChampionBuildDao
    abstract fun itemDao(): ItemDao
}
```

- [ ] **Step 3: Add Migration(3, 4) and ItemDao provider to PersistenceModule.kt**

In `PersistenceModule.kt`, add the migration to the `.addMigrations(...)` call. The current last migration is `Migration(2, 3)`. Add after it:

```kotlin
object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `items` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `plaintext` TEXT NOT NULL,
                `image` TEXT NOT NULL,
                `gold` TEXT NOT NULL,
                `tags` TEXT NOT NULL,
                `maps` TEXT NOT NULL,
                `stats` TEXT NOT NULL,
                `components` TEXT NOT NULL,
                `upgrades` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )""".trimIndent()
        )
    }
}
```

Also add `provideItemDao` at the bottom of the module (after `provideChampionBuildDao`):

```kotlin
@Provides
fun provideItemDao(db: ChampionDataBase): ItemDao = db.itemDao()
```

- [ ] **Step 4: Build to confirm Room compiles without errors**

```
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL — Room should process the new entity and DAO.

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/zzy/champions/data/local/db/ItemDao.kt
git add app/src/main/java/com/zzy/champions/data/local/db/ChampionDataBase.kt
git add app/src/main/java/com/zzy/champions/di/PersistenceModule.kt
git commit -m "feat: add ItemDao, bump DB to v4 with items table migration"
```

---

## Task 4: API Endpoint

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/data/remote/Api.kt`

- [ ] **Step 1: Add getItems() to Api.kt**

Add this method to the `Api` interface after the existing `getChampionDetail`:

```kotlin
@GET("/cdn/{version}/data/{language}/item.json")
suspend fun getItems(
    @Path("version") version: String,
    @Path("language") language: String
): ItemResponse
```

Also add the import at the top:
```kotlin
import com.zzy.champions.data.model.ItemResponse
```

- [ ] **Step 2: Build to confirm it compiles**

```
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```
git add app/src/main/java/com/zzy/champions/data/remote/Api.kt
git commit -m "feat: add getItems() endpoint to Api"
```

---

## Task 5: ItemRepository

**Files:**
- Create: `app/src/main/java/com/zzy/champions/data/repository/ItemRepository.kt`
- Create: `app/src/main/java/com/zzy/champions/data/repository/DefaultItemRepository.kt`
- Create: `app/src/test/java/com/zzy/champions/TestItemRepository.kt`
- Create: `app/src/test/java/com/zzy/champions/data/repository/DefaultItemRepositoryTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// app/src/test/java/com/zzy/champions/data/repository/DefaultItemRepositoryTest.kt
package com.zzy.champions.data.repository

import com.zzy.champions.data.local.db.ItemDao
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.data.model.ItemResponse
import com.zzy.champions.data.remote.Api
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DefaultItemRepositoryTest {

    @MockK private lateinit var api: Api
    @MockK private lateinit var dao: ItemDao
    private lateinit var repository: DefaultItemRepository

    private val fakeItemFromApi = Item(
        id = "",   // no id in JSON body — set from map key
        name = "Long Sword",
        description = "A sword",
        plaintext = "Slightly increases Attack Damage",
        image = Image("1036.png"),
        gold = ItemGold(base = 350, purchasable = true, total = 350, sell = 245),
        tags = listOf("Damage"),
        maps = mapOf("11" to true),
        stats = mapOf("FlatPhysicalDamageMod" to 10.0),
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = DefaultItemRepository(api, dao, Dispatchers.Unconfined)
    }

    @Test
    fun getRemoteItems_setsIdFromMapKey() = runTest {
        coEvery { api.getItems("14.1.1", "en_US") } returns ItemResponse(mapOf("1036" to fakeItemFromApi))

        val result = repository.getRemoteItems("14.1.1", "en_US")

        assertEquals("1036", result.first().id)
    }

    @Test
    fun saveLocalItems_callsDao() = runTest {
        val item = fakeItemFromApi.copy(id = "1036")
        coJustRun { dao.insertItems(listOf(item)) }

        repository.saveLocalItems(listOf(item))

        coVerify { dao.insertItems(listOf(item)) }
    }

    @Test
    fun getLocalItems_returnsAllFromDao() = runTest {
        val item = fakeItemFromApi.copy(id = "1036")
        coEvery { dao.getAllItems() } returns listOf(item)

        assertEquals(listOf(item), repository.getLocalItems())
    }

    @Test
    fun getItemCount_returnsDaoCount() = runTest {
        coEvery { dao.getItemCount() } returns 200

        assertEquals(200, repository.getItemCount())
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.data.repository.DefaultItemRepositoryTest"
```
Expected: FAIL — `DefaultItemRepository` not found.

- [ ] **Step 3: Create ItemRepository interface**

```kotlin
// app/src/main/java/com/zzy/champions/data/repository/ItemRepository.kt
package com.zzy.champions.data.repository

import com.zzy.champions.data.model.Item

interface ItemRepository {
    suspend fun getRemoteItems(version: String, language: String): List<Item>
    suspend fun saveLocalItems(items: List<Item>)
    suspend fun getLocalItems(): List<Item>
    suspend fun getItemCount(): Int
    suspend fun getItemById(id: String): Item?
}
```

- [ ] **Step 4: Create DefaultItemRepository**

```kotlin
// app/src/main/java/com/zzy/champions/data/repository/DefaultItemRepository.kt
package com.zzy.champions.data.repository

import com.zzy.champions.data.local.db.ItemDao
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.Api
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultItemRepository @Inject constructor(
    private val api: Api,
    private val dao: ItemDao,
    private val dispatcher: CoroutineDispatcher,
) : ItemRepository {

    override suspend fun getRemoteItems(version: String, language: String): List<Item> =
        withContext(dispatcher) {
            api.getItems(version, language).data.map { (id, item) -> item.copy(id = id) }
        }

    override suspend fun saveLocalItems(items: List<Item>) = withContext(dispatcher) {
        dao.insertItems(items)
    }

    override suspend fun getLocalItems(): List<Item> = withContext(dispatcher) {
        dao.getAllItems()
    }

    override suspend fun getItemCount(): Int = withContext(dispatcher) {
        dao.getItemCount()
    }

    override suspend fun getItemById(id: String): Item? = withContext(dispatcher) {
        dao.getItemById(id)
    }
}
```

- [ ] **Step 5: Create TestItemRepository (test helper)**

```kotlin
// app/src/test/java/com/zzy/champions/TestItemRepository.kt
package com.zzy.champions

import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.data.repository.ItemRepository

internal val longSword = Item(
    id = "1036",
    name = "Long Sword",
    description = "A long sword",
    plaintext = "Slightly increases Attack Damage",
    image = Image("1036.png"),
    gold = ItemGold(base = 350, purchasable = true, total = 350, sell = 245),
    tags = listOf("Damage"),
    maps = mapOf("11" to true, "12" to true),
    stats = mapOf("FlatPhysicalDamageMod" to 10.0),
    components = emptyList(),
    upgrades = listOf("3153"),
)

internal val infinityEdge = Item(
    id = "3031",
    name = "Infinity Edge",
    description = "Critical strikes deal bonus damage",
    plaintext = "Greatly increases Attack Damage and Critical Strike Damage",
    image = Image("3031.png"),
    gold = ItemGold(base = 625, purchasable = true, total = 3400, sell = 2380),
    tags = listOf("Damage", "CriticalStrike", "Legendary"),
    maps = mapOf("11" to true, "12" to true),
    stats = mapOf("FlatPhysicalDamageMod" to 80.0, "FlatCritChanceMod" to 0.2),
    components = listOf("1038", "1018"),
    upgrades = emptyList(),
)

internal val sorceresShoes = Item(
    id = "3020",
    name = "Sorcerer's Shoes",
    description = "Increases magic penetration",
    plaintext = "Enhances Move Speed and magic penetration",
    image = Image("3020.png"),
    gold = ItemGold(base = 600, purchasable = true, total = 1100, sell = 770),
    tags = listOf("Boots", "SpellDamage"),
    maps = mapOf("11" to true, "12" to true),
    stats = mapOf("FlatMovementSpeedMod" to 45.0, "FlatMagicPenetrationMod" to 18.0),
    components = listOf("1001"),
    upgrades = emptyList(),
)

internal class TestItemRepository : ItemRepository {
    private val items = mutableListOf(longSword, infinityEdge, sorceresShoes)
    var shouldThrowOnFetch = false

    override suspend fun getRemoteItems(version: String, language: String): List<Item> {
        if (shouldThrowOnFetch) throw java.io.IOException("Network error")
        return items
    }

    override suspend fun saveLocalItems(items: List<Item>) {
        this.items.clear()
        this.items.addAll(items)
    }

    override suspend fun getLocalItems(): List<Item> = items.toList()

    override suspend fun getItemCount(): Int = items.size

    override suspend fun getItemById(id: String): Item? = items.find { it.id == id }
}
```

- [ ] **Step 6: Run tests to confirm they pass**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.data.repository.DefaultItemRepositoryTest"
```
Expected: PASS (4 tests)

- [ ] **Step 7: Commit**

```
git add app/src/main/java/com/zzy/champions/data/repository/ItemRepository.kt
git add app/src/main/java/com/zzy/champions/data/repository/DefaultItemRepository.kt
git add app/src/test/java/com/zzy/champions/TestItemRepository.kt
git add app/src/test/java/com/zzy/champions/data/repository/DefaultItemRepositoryTest.kt
git commit -m "feat: add ItemRepository and DefaultItemRepository"
```

---

## Task 6: GetItemDataUseCase

**Files:**
- Create: `app/src/main/java/com/zzy/champions/domain/GetItemDataUseCase.kt`
- Create: `app/src/test/java/com/zzy/champions/domain/GetItemDataUseCaseTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// app/src/test/java/com/zzy/champions/domain/GetItemDataUseCaseTest.kt
package com.zzy.champions.domain

import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.TestItemRepository
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.infinityEdge
import com.zzy.champions.longSword
import com.zzy.champions.sorceresShoes
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetItemDataUseCaseTest {

    @MockK private lateinit var appDataRepository: AppDataRepository
    private lateinit var itemRepository: TestItemRepository
    private lateinit var useCase: GetItemDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appDataRepository.getLocalVersion() } returns flowOf(VERSION_14_0)
        coEvery { appDataRepository.getLanguage() } returns flowOf(LANGUAGE_US)
        itemRepository = TestItemRepository()
        useCase = GetItemDataUseCase(itemRepository, appDataRepository, Dispatchers.Unconfined)
    }

    @Test
    fun invoke_whenItemsCached_returnsSuccessWithCachedItems() = runTest {
        // TestItemRepository starts with 3 items seeded
        val result = useCase()

        assertTrue(result is UiState.Success)
        assertEquals(3, (result as UiState.Success).data.size)
    }

    @Test
    fun invoke_whenItemsEmpty_fetchesFromApiAndReturnsSuccess() = runTest {
        itemRepository.saveLocalItems(emptyList())

        val result = useCase()

        assertTrue(result is UiState.Success)
        assertEquals(3, (result as UiState.Success).data.size)  // re-fetched from fake remote
    }

    @Test
    fun invoke_whenNetworkFails_andNoCachedItems_returnsError() = runTest {
        itemRepository.saveLocalItems(emptyList())
        itemRepository.shouldThrowOnFetch = true

        val result = useCase()

        assertTrue(result is UiState.Error)
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.domain.GetItemDataUseCaseTest"
```
Expected: FAIL — `GetItemDataUseCase` not found.

- [ ] **Step 3: Create GetItemDataUseCase.kt**

```kotlin
// app/src/main/java/com/zzy/champions/domain/GetItemDataUseCase.kt
package com.zzy.champions.domain

import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.data.repository.ItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetItemDataUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
    private val appDataRepository: AppDataRepository,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(): UiState<List<Item>> = withContext(dispatcher) {
        try {
            val cachedCount = itemRepository.getItemCount()
            if (cachedCount > 0) {
                return@withContext UiState.Success(itemRepository.getLocalItems())
            }
            val version = appDataRepository.getLocalVersion().first()
            val language = appDataRepository.getLanguage().first()
            val fetched = itemRepository.getRemoteItems(version, language)
                .filter { it.gold.purchasable }
            itemRepository.saveLocalItems(fetched)
            UiState.Success(itemRepository.getLocalItems())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            val cached = itemRepository.getLocalItems()
            if (cached.isNotEmpty()) UiState.Success(cached) else UiState.Error(e)
        }
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.domain.GetItemDataUseCaseTest"
```
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/zzy/champions/domain/GetItemDataUseCase.kt
git add app/src/test/java/com/zzy/champions/domain/GetItemDataUseCaseTest.kt
git commit -m "feat: add GetItemDataUseCase with fetch-if-empty strategy"
```

---

## Task 7: DI Wiring

**Files:**
- Modify: `app/src/main/java/com/zzy/champions/di/RepositoryModule.kt`

- [ ] **Step 1: Add ItemRepository binding to RepositoryModule.kt**

Add this binding to the existing `RepositoryModule` interface:

```kotlin
@Binds
fun bindItemRepository(itemRepository: DefaultItemRepository): ItemRepository
```

Also add these imports at the top:
```kotlin
import com.zzy.champions.data.repository.DefaultItemRepository
import com.zzy.champions.data.repository.ItemRepository
```

- [ ] **Step 2: Build to confirm DI wires up**

```
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL — Hilt can now inject `ItemRepository`.

- [ ] **Step 3: Commit**

```
git add app/src/main/java/com/zzy/champions/di/RepositoryModule.kt
git commit -m "feat: wire ItemRepository into Hilt DI"
```

---

## Task 8: ItemViewModel

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt`
- Create: `app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt
package com.zzy.champions.items

import com.zzy.champions.MainDispatcherRule
import com.zzy.champions.TestItemRepository
import com.zzy.champions.LANGUAGE_US
import com.zzy.champions.VERSION_14_0
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.data.repository.AppDataRepository
import com.zzy.champions.domain.GetItemDataUseCase
import com.zzy.champions.infinityEdge
import com.zzy.champions.longSword
import com.zzy.champions.ui.items.ItemViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        useCase = GetItemDataUseCase(itemRepository, appDataRepository, mainDispatcherRule.testDispatcher)
        viewModel = ItemViewModel(useCase)
    }

    @Test
    fun stateIsInitiallyLoading() {
        assertEquals(UiState.Loading, viewModel.items.value)
    }

    @Test
    fun items_loadsSuccessfully() = runTest {
        val job = launch { viewModel.items.collect() }
        advanceUntilIdle()

        val state = viewModel.items.value
        assert(state is UiState.Success)
        assertEquals(3, (state as UiState.Success).data.size)
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
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"
```
Expected: FAIL — `ItemViewModel` not found.

- [ ] **Step 3: Create ItemViewModel.kt**

```kotlin
// app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt
package com.zzy.champions.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.domain.GetItemDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val getItemDataUseCase: GetItemDataUseCase,
) : ViewModel() {

    val items: StateFlow<UiState<List<Item>>> = flow {
        emit(getItemDataUseCase())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem.asStateFlow()

    fun selectItem(item: Item) { _selectedItem.value = item }
    fun dismissItem() { _selectedItem.value = null }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.items.ItemViewModelTest"
```
Expected: PASS (5 tests)

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/zzy/champions/ui/items/ItemViewModel.kt
git add app/src/test/java/com/zzy/champions/items/ItemViewModelTest.kt
git commit -m "feat: add ItemViewModel with items state and bottom sheet selection"
```

---

## Task 9: ItemCard Composable

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemCard.kt`

- [ ] **Step 1: Create ItemCard.kt**

```kotlin
// app/src/main/java/com/zzy/champions/ui/items/compose/ItemCard.kt
package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zzy.champions.data.model.Item

@Composable
fun ItemCard(
    item: Item,
    version: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = item.getIconUrl(version),
            contentDescription = item.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = "${item.gold.total}g",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **Step 2: Build to confirm it compiles**

```
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemCard.kt
git commit -m "feat: add ItemCard composable"
```

---

## Task 10: ItemBottomSheet Composable

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemBottomSheet.kt`

The bottom sheet shows: icon + name + gold, divider, stat lines, description, build-path icons (tappable to navigate to that item).

Stats display: strip `Flat`/`Percent`/`Mod` from key names and insert spaces before capitals.
Example: `FlatPhysicalDamageMod` → `Physical Damage`

- [ ] **Step 1: Create ItemBottomSheet.kt**

```kotlin
// app/src/main/java/com/zzy/champions/ui/items/compose/ItemBottomSheet.kt
package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zzy.champions.data.model.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBottomSheet(
    item: Item,
    version: String,
    onDismiss: () -> Unit,
    onComponentClick: (String) -> Unit,   // item id of tapped component
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartialExpansion = false),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header: icon + name + cost
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = item.getIconUrl(version),
                    contentDescription = item.name,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${item.gold.total}g  ·  ${item.tags.firstOrNull() ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Stats
            item.stats.forEach { (key, value) ->
                val label = formatStatKey(key)
                val display = if (value < 1.0) "+${(value * 100).toInt()}%" else "+${value.toInt()}"
                Text(
                    text = "$display $label",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            // Description (use plaintext if description is empty, otherwise description)
            val descText = item.description
                .replace(Regex("<[^>]+>"), "")  // strip HTML tags
                .trim()
                .ifEmpty { item.plaintext }
            if (descText.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(text = descText, style = MaterialTheme.typography.bodySmall)
            }

            // Build path — components
            if (item.components.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(text = "Builds from:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.components.forEach { componentId ->
                        AsyncImage(
                            model = "https://ddragon.leagueoflegends.com/cdn/$version/img/item/$componentId.png",
                            contentDescription = componentId,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onComponentClick(componentId) },
                        )
                    }
                }
            }

            // Upgrades — what this item builds into
            if (item.upgrades.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(text = "Builds into:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.upgrades.forEach { upgradeId ->
                        AsyncImage(
                            model = "https://ddragon.leagueoflegends.com/cdn/$version/img/item/$upgradeId.png",
                            contentDescription = upgradeId,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onComponentClick(upgradeId) },
                        )
                    }
                }
            }
        }
    }
}

private fun formatStatKey(key: String): String =
    key.replace(Regex("^(Flat|Percent)"), "")
        .replace(Regex("Mod$"), "")
        .replace(Regex("([A-Z])"), " $1")
        .trim()
```

- [ ] **Step 2: Build to confirm it compiles**

```
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemBottomSheet.kt
git commit -m "feat: add ItemBottomSheet composable with stats, description, build path"
```

---

## Task 11: ItemScreen

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt`
- Create: `app/src/test/java/com/zzy/champions/items/ItemCategorizationTest.kt`

The screen groups items into named sections and renders them in a 3-column `LazyVerticalGrid` with full-width category headers.

- [ ] **Step 1: Write failing tests for the category logic**

```kotlin
// app/src/test/java/com/zzy/champions/items/ItemCategorizationTest.kt
package com.zzy.champions.items

import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.ui.items.compose.categorizeItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemCategorizationTest {

    private fun item(id: String, tags: List<String>, total: Int, components: List<String> = emptyList(), purchasable: Boolean = true) = Item(
        id = id, name = id, description = "", plaintext = "",
        image = Image("$id.png"),
        gold = ItemGold(total = total, purchasable = purchasable),
        tags = tags,
        maps = mapOf("11" to true),
        stats = emptyMap(),
        components = components,
    )

    @Test
    fun bootsTagged_goesToBootsCategory() {
        val boots = item("3020", listOf("Boots"), 1100)
        val result = categorizeItems(listOf(boots))
        val bootsCat = result.find { it.first == "Boots" }
        assertTrue(bootsCat?.second?.contains(boots) == true)
    }

    @Test
    fun mythicTagged_goesToMythicCategory() {
        val mythic = item("6632", listOf("Damage", "Mythic"), 3300)
        val result = categorizeItems(listOf(mythic))
        val mythicCat = result.find { it.first == "Mythic" }
        assertTrue(mythicCat?.second?.contains(mythic) == true)
    }

    @Test
    fun legendaryTagged_goesToLegendaryCategory() {
        val legendary = item("3031", listOf("Damage", "Legendary"), 3400)
        val result = categorizeItems(listOf(legendary))
        val legCat = result.find { it.first == "Legendary" }
        assertTrue(legCat?.second?.contains(legendary) == true)
    }

    @Test
    fun itemUsedAsComponent_goesToComponentsCategory() {
        val component = item("1036", listOf("Damage"), 350)
        val parent = item("3153", listOf("Damage", "Legendary"), 3300, components = listOf("1036"))
        val result = categorizeItems(listOf(component, parent))
        val compCat = result.find { it.first == "Components" }
        assertTrue(compCat?.second?.contains(component) == true)
    }

    @Test
    fun lowCostNonComponent_goesToStarterCategory() {
        val starter = item("1054", listOf("Health"), 400)  // Doran's Shield — not a component
        val result = categorizeItems(listOf(starter))
        val starterCat = result.find { it.first == "Starter" }
        assertTrue(starterCat?.second?.contains(starter) == true)
    }

    @Test
    fun eachItem_appearsInExactlyOneCategory() {
        val items = listOf(
            item("3031", listOf("Damage", "Legendary"), 3400),
            item("1036", listOf("Damage"), 350),
            item("3153", listOf("Damage", "Legendary"), 3300, components = listOf("1036")),
            item("3020", listOf("Boots"), 1100),
            item("1054", listOf("Health"), 400),
        )
        val result = categorizeItems(items)
        val allAssigned = result.flatMap { it.second }
        assertEquals(items.size, allAssigned.size)
        assertEquals(items.size, allAssigned.distinctBy { it.id }.size)
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.items.ItemCategorizationTest"
```
Expected: FAIL — `categorizeItems` not found.

- [ ] **Step 3: Create ItemScreen.kt with categorizeItems function**

```kotlin
// app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt
package com.zzy.champions.ui.items.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zzy.champions.data.model.Item
import com.zzy.champions.data.remote.UiState
import com.zzy.champions.ui.detail.compose.LoadingAndErrorScreen
import com.zzy.champions.ui.items.ItemViewModel

private const val GRID_COLUMNS = 3

@Composable
fun ItemRoute(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = hiltViewModel(),
) {
    val itemsState by viewModel.items.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val version = "14.24.1"  // TODO: wire from AppDataRepository in a follow-up

    ItemScreen(
        modifier = modifier,
        itemsState = itemsState,
        version = version,
        onItemClick = viewModel::selectItem,
    )

    selectedItem?.let { item ->
        ItemBottomSheet(
            item = item,
            version = version,
            onDismiss = viewModel::dismissItem,
            onComponentClick = { componentId ->
                // find the component item and show its sheet
                if (itemsState is UiState.Success) {
                    val component = (itemsState as UiState.Success).data.find { it.id == componentId }
                    component?.let { viewModel.selectItem(it) }
                }
            },
        )
    }
}

@Composable
fun ItemScreen(
    modifier: Modifier = Modifier,
    itemsState: UiState<List<Item>>,
    version: String,
    onItemClick: (Item) -> Unit,
) {
    when (itemsState) {
        is UiState.Loading -> LoadingAndErrorScreen(
            isLoading = true,
            isError = false,
            onReloadClick = {},
            modifier = modifier,
        )
        is UiState.Error -> LoadingAndErrorScreen(
            isLoading = false,
            isError = true,
            onReloadClick = {},
            modifier = modifier,
        )
        is UiState.Success -> {
            val categories = categorizeItems(itemsState.data)
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                modifier = modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                categories.forEach { (categoryName, categoryItems) ->
                    item(span = { GridItemSpan(GRID_COLUMNS) }) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(categoryItems, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            version = version,
                            onClick = { onItemClick(item) },
                        )
                    }
                }
            }
        }
    }
}

fun categorizeItems(items: List<Item>): List<Pair<String, List<Item>>> {
    val allItemIds = items.map { it.id }.toSet()
    val componentIds = items.flatMap { it.components }.filter { it in allItemIds }.toSet()

    val categories = listOf(
        "Starter"    to { item: Item -> item.gold.total <= 500 && item.id !in componentIds },
        "Boots"      to { item: Item -> "Boots" in item.tags },
        "Mythic"     to { item: Item -> "Mythic" in item.tags },
        "Legendary"  to { item: Item -> "Legendary" in item.tags },
        "Components" to { item: Item -> item.id in componentIds && "Mythic" !in item.tags && "Legendary" !in item.tags },
        "Epic"       to { item: Item -> item.gold.total in 1000..2999 },
        "Other"      to { _: Item -> true },
    )

    val assigned = mutableSetOf<String>()
    return categories.mapNotNull { (name, predicate) ->
        val batch = items.filter { it.id !in assigned && predicate(it) }
        if (batch.isEmpty()) null
        else {
            assigned.addAll(batch.map { it.id })
            name to batch
        }
    }
}
```

- [ ] **Step 4: Run categorization tests to confirm they pass**

```
./gradlew :app:testDebugUnitTest --tests "com.zzy.champions.items.ItemCategorizationTest"
```
Expected: PASS (6 tests)

- [ ] **Step 5: Commit**

```
git add app/src/main/java/com/zzy/champions/ui/items/compose/ItemScreen.kt
git add app/src/test/java/com/zzy/champions/items/ItemCategorizationTest.kt
git commit -m "feat: add ItemScreen with category sections and categorizeItems logic"
```

---

## Task 12: Navigation + Bottom Nav Bar

**Files:**
- Create: `app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt`
- Modify: `app/src/main/java/com/zzy/champions/ui/ChampionApp.kt`

- [ ] **Step 1: Create ItemsNavigation.kt**

```kotlin
// app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt
package com.zzy.champions.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zzy.champions.ui.items.compose.ItemRoute

const val ITEMS_ROUTE = "items"

fun NavGraphBuilder.itemsScreen() {
    composable(route = ITEMS_ROUTE) {
        ItemRoute()
    }
}
```

- [ ] **Step 2: Add itemsScreen() to ChampionNavHost**

In `ChampionNavHost.kt`, add `itemsScreen()` after `languageScreen(...)`:

```kotlin
itemsScreen()
```

No other changes to ChampionNavHost are needed.

- [ ] **Step 3: Update ChampionApp.kt to add bottom NavigationBar**

Replace the full content of `ChampionApp.kt`:

```kotlin
// app/src/main/java/com/zzy/champions/ui/ChampionApp.kt
package com.zzy.champions.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zzy.champions.R
import com.zzy.champions.ui.navigation.CHAMPION_INDEX_ROUTE
import com.zzy.champions.ui.navigation.ChampionNavHost
import com.zzy.champions.ui.navigation.ITEMS_ROUTE

private val TOP_LEVEL_ROUTES = setOf(CHAMPION_INDEX_ROUTE, ITEMS_ROUTE)

@Composable
fun ChampionApp(
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == CHAMPION_INDEX_ROUTE,
                        onClick = { navController.navigateSingleTopTo(CHAMPION_INDEX_ROUTE) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_champions),
                                contentDescription = "Champions",
                            )
                        },
                        label = { Text("Champions") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == ITEMS_ROUTE,
                        onClick = { navController.navigateSingleTopTo(ITEMS_ROUTE) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_items),
                                contentDescription = "Items",
                            )
                        },
                        label = { Text("Items") },
                    )
                }
            }
        },
    ) { padding ->
        ChampionNavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            onLinkClick = onLinkClick,
        )
    }
}
```

- [ ] **Step 4: Add navigation icons to drawable resources**

The `NavigationBarItem` icons reference `R.drawable.ic_champions` and `R.drawable.ic_items`. Add two vector drawables:

```xml
<!-- app/src/main/res/drawable/ic_champions.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurface"
        android:pathData="M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13C19,5.13 15.87,2 12,2zM12,11.5c-1.38,0 -2.5,-1.12 -2.5,-2.5s1.12,-2.5 2.5,-2.5 2.5,1.12 2.5,2.5 -1.12,2.5 -2.5,2.5z"/>
</vector>
```

```xml
<!-- app/src/main/res/drawable/ic_items.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="?attr/colorOnSurface"
        android:pathData="M20,6h-2.18c0.07,-0.44 0.18,-0.86 0.18,-1.3C18,2.55 16.45,1 14.5,1c-1.47,0 -2.67,0.87 -3.34,2.07L12,4.07l-1.16,-0.99C10.17,1.87 8.97,1 7.5,1 5.55,1 4,2.55 4,4.5c0,0.44 0.11,0.86 0.18,1.3L2,6c-1.1,0 -1.99,0.9 -1.99,2L0,19c0,1.1 0.9,2 2,2h18c1.1,0 2,-0.9 2,-2L22,8c0,-1.1 -0.9,-2 -2,-2zM20,19L4,19v-2l8,-5 8,5v2zM20,10l-8,5 -8,-5V8l8,5 8,-5v2z"/>
</vector>
```

- [ ] **Step 5: Build the full app**

```
./gradlew :app:assembleDebug
```
Expected: BUILD SUCCESSFUL — no compilation errors.

- [ ] **Step 6: Run all unit tests**

```
./gradlew :app:testDebugUnitTest
```
Expected: All existing tests plus new item tests pass.

- [ ] **Step 7: Commit**

```
git add app/src/main/java/com/zzy/champions/ui/navigation/ItemsNavigation.kt
git add app/src/main/java/com/zzy/champions/ui/navigation/ChampionNavHost.kt
git add app/src/main/java/com/zzy/champions/ui/ChampionApp.kt
git add app/src/main/res/drawable/ic_champions.xml
git add app/src/main/res/drawable/ic_items.xml
git commit -m "feat: add items navigation route and bottom nav bar"
```

---

## Verification Checklist

After all tasks:

- [ ] `./gradlew :app:testDebugUnitTest` — all tests pass
- [ ] `./gradlew :app:assembleDebug` — app builds without errors
- [ ] Install on device/emulator — bottom nav shows Champions and Items tabs
- [ ] Items tab loads items grouped by Starter / Boots / Mythic / Legendary / Components / Epic / Other
- [ ] Tapping an item opens the bottom sheet with stats, description, and build path icons
- [ ] Tapping a build-path icon in the sheet navigates to that component's sheet
- [ ] Tapping Champions tab returns to the champion index with state restored
- [ ] Settings screen still accessible from the champion index top bar
- [ ] Champion detail navigation still works from the champion index

---

## Known Follow-ups (not in scope)

- Wire `version` in `ItemRoute` from `AppDataRepository` instead of the hardcoded string
- Add search bar to `ItemScreen` (same pattern as champion index)
- Add version-based cache invalidation to `GetItemDataUseCase` (currently fetch-if-empty only)
- Full stat key → display name mapping table
