# Items Page — Phase 1 Design Spec

**Date:** 2026-06-07
**Status:** Approved
**Scope:** Standalone items browser (Phase 1 of 3)

---

## Goals

Add a standalone items page to the lol_champs Android app, allowing players to browse all League of Legends items organized by category, view item details in a bottom sheet, and access the page via a new bottom navigation bar.

**Out of scope for Phase 1:**
- Champion detail page integration (deferred to Phase C builds tab)
- Search bar (planned for a future iteration)
- Per-champion item sets / build editor (Phase C)

**Long-term goal (Phase C):** Full build editor on champion detail — pick items from a searchable overlay, save named item sets per champion in Room DB.

---

## Data Source

Data Dragon API: `/cdn/{version}/data/{language}/item.json`

Returns a flat `Map<String, ItemDetail>` (key = item ID string, e.g. `"3153"`). Each item includes:

| Field | Type | Notes |
|---|---|---|
| `name` | String | Display name |
| `description` | String | HTML-tagged; strip tags at parse time |
| `plaintext` | String | Short summary |
| `image.full` | String | Icon filename |
| `gold.total` | Int | Total gold cost |
| `gold.purchasable` | Boolean | Whether buyable in shop |
| `tags` | List\<String\> | e.g. `["Damage","CriticalStrike"]` |
| `maps` | Map\<String, Boolean\> | `"11"`=SR, `"12"`=ARAM |
| `stats` | Map\<String, Float\> | e.g. `{"FlatPhysicalDamageMod": 30.0}` |
| `from` | List\<String\> | Component item IDs |
| `into` | List\<String\> | Upgrade item IDs |

No "popular items per champion" data exists in Data Dragon. Third-party APIs (OP.GG, U.GG) would be required for that — out of scope.

---

## Architecture

Follows the existing Clean Architecture layered pattern exactly. No new patterns introduced.

### Remote
- `Api.kt` — add `getItems(version: String, language: String): ItemResponse`
- `ItemResponse.kt` — `data: Map<String, Item>` Moshi-annotated wrapper

### Data Layer
- `Item.kt` — `@Entity(tableName = "items")`, `@JsonClass(generateAdapter = true)`
- `ItemDao.kt` — `insertItems()`, `getAllItems()`, `clearItems()`
- `DefaultItemRepository.kt` — version-check pattern identical to `DefaultChampionRepository`:
  1. Compare remote version with cached local version
  2. If stale: fetch from API → save to DB → update version
  3. If fresh: return from DB
  4. On network failure: fall back to local DB

### Domain Layer
- `GetItemDataUseCase.kt` — orchestrates version check → fetch → save → emit `UiState<List<Item>>`

### UI Layer
- `ItemViewModel.kt` — exposes:
  - `items: StateFlow<UiState<List<Item>>>`
  - `selectedItem: StateFlow<Item?>` (drives bottom sheet open/close)
- `ItemScreen.kt` and sub-composables (see UI section below)

### Navigation
- New `itemsScreen` composable + `ItemsNavigation.kt`
- Route: `"items"`
- Entry: bottom navigation bar (new)

---

## Database Migration: v3 → v4

One new table. No existing tables modified. No data loss risk.

```sql
CREATE TABLE IF NOT EXISTS `items` (
    `id` TEXT NOT NULL,
    `name` TEXT NOT NULL,
    `description` TEXT NOT NULL,
    `plaintext` TEXT NOT NULL,
    `imageFull` TEXT NOT NULL,
    `goldTotal` INTEGER NOT NULL,
    `purchasable` INTEGER NOT NULL,
    `tags` TEXT NOT NULL,
    `maps` TEXT NOT NULL,
    `stats` TEXT NOT NULL,
    `from` TEXT NOT NULL,
    `into` TEXT NOT NULL,
    PRIMARY KEY(`id`)
)
```

TypeConverters for `List<String>`, `Map<String, Boolean>`, and `Map<String, Float>` follow the existing `Converters.kt` pattern.

---

## UI Design

### Bottom Navigation Bar

Wraps the existing `ChampionNavHost` in a `Scaffold` with a `NavigationBar`. Two tabs:

```
│  [⚔ Champions]    [🛡 Items]  │
```

Settings remains accessible via a top-bar icon on either tab (unchanged).

Scales to Phase C: a third "Builds" tab can be added without restructuring.

### Items Screen

```
┌─────────────────────────────────┐
│  Items                  [🔍]   │  ← search icon (inactive, Phase 2)
├─────────────────────────────────┤
│  Starter Items                  │  ← section header
│  ┌──────┐ ┌──────┐ ┌──────┐   │
│  │ icon │ │ icon │ │ icon │   │  ← 3-column LazyVerticalGrid
│  │ name │ │ name │ │ name │   │
│  │ 350g │ │ 400g │ │ 500g │   │
│  └──────┘ └──────┘ └──────┘   │
│                                 │
│  Boots                          │
│  ┌──────┐ ┌──────┐             │
│  ...                            │
│  Legendary Items                │
│  ...                            │
└─────────────────────────────────┘
│  [Champions]    [Items]         │
```

**Category ordering** (mirrors in-game shop). Each item is assigned to the **first matching** category in this priority order:
1. Starter Items — `purchasable=true`, `goldTotal <= 500`
2. Boots — has tag `Boots`
3. Mythic — has tag `Mythic`
4. Legendary — has tag `Legendary`
5. Components — appears in the `from` list of any other item AND has neither `Mythic` nor `Legendary` tag
6. Epic — `goldTotal` between 1000–2999, not matched above
7. Other — everything else (consumables, trinkets, jungle items, uncategorized)

Items with `purchasable=false` are excluded from display.
Items not available on Summoner's Rift (`maps["11"] == false`) are shown in an "ARAM / Other Maps" subsection under Other, or hidden based on a future filter toggle.

### Item Card

```
┌──────────┐
│  [icon]  │
│  Name    │
│  1200g   │
└──────────┘
```

- Icon loaded via Coil from `https://ddragon.leagueoflegends.com/cdn/{version}/img/item/{imageFull}`
- Name truncated to 2 lines
- Gold cost in secondary text

### Item Bottom Sheet

Opens on item card tap. Uses `ModalBottomSheet`.

```
┌─────────────────────────────────┐
│  ▬▬▬  (drag handle)             │
│  ┌────┐  Infinity Edge          │
│  │icon│  3400g  · Damage        │
│  └────┘                         │
│  ─────────────────────────────  │
│  +80 Attack Damage              │
│  +20% Critical Strike Chance    │
│                                 │
│  Passive: Critical strikes deal │
│  an additional 35% damage.      │
│                                 │
│  Builds from:                   │
│  ┌────┐ ┌────┐  +  850g        │
│  │icon│ │icon│  (component     │
│  └────┘ └────┘   icons)        │
│                                 │
│  Builds into: —                 │
└─────────────────────────────────┘
```

- Description: HTML tags stripped at parse time (Jsoup or regex)
- Stats: rendered with basic key cleanup — strip `Flat`/`Percent`/`Mod` affixes and insert spaces before capitals (e.g. `FlatPhysicalDamageMod` → `Physical Damage`). Full stat-name mapping table is deferred to Phase 2.
- Build path icons are tappable — tapping a component opens that item's bottom sheet
- `from`/`into` sections hidden if lists are empty

---

## Error Handling

Uses the existing `UiState<T>` sealed class and `LoadingAndErrorScreen` composable:
- `UiState.Loading` → skeleton / spinner
- `UiState.Error` → `LoadingAndErrorScreen` with retry action
- `UiState.Success` → item category sections

---

## What Phase 1 Does NOT Include

| Feature | Phase |
|---|---|
| Search bar on items page | Phase 2 |
| Tag/role filter chips | Phase 2 |
| Champion detail "Items" section | Phase C |
| Per-champion item sets (build editor) | Phase C |
| ARAM/other map filter toggle | Phase 2+ |

---

## Files to Create / Modify

### New files
- `data/model/Item.kt`
- `data/model/ItemResponse.kt`
- `data/local/db/ItemDao.kt`
- `data/repository/ItemRepository.kt`
- `data/repository/DefaultItemRepository.kt`
- `domain/GetItemDataUseCase.kt`
- `ui/items/ItemViewModel.kt`
- `ui/items/compose/ItemScreen.kt`
- `ui/items/compose/ItemCard.kt`
- `ui/items/compose/ItemBottomSheet.kt`
- `ui/navigation/ItemsNavigation.kt`

### Modified files
- `data/remote/Api.kt` — add `getItems()` endpoint
- `data/local/db/ChampionDataBase.kt` — bump version to 4, add `Item::class`, add migration
- `ui/navigation/ChampionNavHost.kt` — add bottom nav bar + items route
- DI modules — bind `ItemRepository`, provide `ItemDao`, `GetItemDataUseCase`
