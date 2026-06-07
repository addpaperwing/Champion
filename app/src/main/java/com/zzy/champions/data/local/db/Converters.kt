package com.zzy.champions.data.local.db

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.ItemGold
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Spell
import com.zzy.champions.data.model.Stats
import com.zzy.champions.data.remote.BigDecimalAdapter

@OptIn(ExperimentalStdlibApi::class)
object Converters {

    // Moshi is expensive to build and is thread-safe, so build it once and reuse the
    // adapters instead of recreating them on every read/write.
    private val moshi: Moshi = Moshi.Builder().add(BigDecimalAdapter).build()

    private val skinNumberListAdapter: JsonAdapter<List<SkinNumber>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, SkinNumber::class.java))
    private val tagListAdapter: JsonAdapter<List<String>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))
    private val spellListAdapter: JsonAdapter<List<Spell>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, Spell::class.java))
    private val infoAdapter: JsonAdapter<Info> = moshi.adapter()
    private val statsAdapter: JsonAdapter<Stats> = moshi.adapter()
    private val imageAdapter: JsonAdapter<Image> = moshi.adapter()
    private val passiveAdapter: JsonAdapter<Passive> = moshi.adapter()

    private val itemGoldAdapter: JsonAdapter<ItemGold> = moshi.adapter()

    private val stringBooleanMapAdapter: JsonAdapter<Map<String, Boolean>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Boolean::class.java))

    private val stringDoubleMapAdapter: JsonAdapter<Map<String, Double>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Double::class.java))

    @TypeConverter
    @JvmStatic
    fun toSkinNumberList(value: String): List<SkinNumber> = skinNumberListAdapter.fromJson(value) ?: emptyList()

    @TypeConverter
    @JvmStatic
    fun fromSkinNumberList(list: List<SkinNumber>): String = skinNumberListAdapter.toJson(list)

    @TypeConverter
    @JvmStatic
    fun toTagList(value: String): List<String> = tagListAdapter.fromJson(value) ?: emptyList()

    @TypeConverter
    @JvmStatic
    fun fromTagList(list: List<String>): String = tagListAdapter.toJson(list)

    @TypeConverter
    @JvmStatic
    fun toInfo(value: String): Info? = infoAdapter.fromJson(value)

    @TypeConverter
    @JvmStatic
    fun fromInfo(info: Info): String = infoAdapter.toJson(info)

    @TypeConverter
    @JvmStatic
    fun toStats(value: String): Stats? = statsAdapter.fromJson(value)

    @TypeConverter
    @JvmStatic
    fun fromStats(stats: Stats): String = statsAdapter.toJson(stats)

    @TypeConverter
    @JvmStatic
    fun toImage(value: String): Image? = imageAdapter.fromJson(value)

    @TypeConverter
    @JvmStatic
    fun fromImage(image: Image): String = imageAdapter.toJson(image)

    @TypeConverter
    @JvmStatic
    fun toSpells(value: String): List<Spell> = spellListAdapter.fromJson(value) ?: emptyList()

    @TypeConverter
    @JvmStatic
    fun fromSpells(list: List<Spell>): String = spellListAdapter.toJson(list)

    @TypeConverter
    @JvmStatic
    fun toPassive(value: String): Passive? = passiveAdapter.fromJson(value)

    @TypeConverter
    @JvmStatic
    fun fromPassive(passive: Passive): String = passiveAdapter.toJson(passive)

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
}
