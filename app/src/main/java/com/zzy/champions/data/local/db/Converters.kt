package com.zzy.champions.data.local.db

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import com.zzy.champions.data.model.Image
import com.zzy.champions.data.model.Info
import com.zzy.champions.data.model.Passive
import com.zzy.champions.data.model.SkinNumber
import com.zzy.champions.data.model.Spell
import com.zzy.champions.data.model.Stats
import com.zzy.champions.data.remote.BigDecimalAdapter
import java.lang.reflect.Type

@OptIn(ExperimentalStdlibApi::class)
object Converters {

    @TypeConverter
    @JvmStatic
    fun toSkinNumberList(value :  String):  List<SkinNumber> {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(
            List::class.java,
            SkinNumber::class.java
        )
        val adapter: JsonAdapter<List<SkinNumber>> = moshi.adapter(type)

        return adapter.fromJson(value)?: emptyList()
    }

    @TypeConverter
    @JvmStatic
    fun fromSkinNumberList(list: List<SkinNumber>): String {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(
            List::class.java,
            SkinNumber::class.java
        )
        val adapter: JsonAdapter<List<SkinNumber>> = moshi.adapter(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun toTagList(value :  String):  List<String> {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
        val adapter: JsonAdapter<List<String>> = moshi.adapter(type)

        return adapter.fromJson(value)?: emptyList()
    }

    @TypeConverter
    @JvmStatic
    fun fromTagList(list: List<String>): String {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
        val adapter: JsonAdapter<List<String>> = moshi.adapter(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun toInfo(value :  String):  Info? {
        val moshi = Moshi.Builder().build()

        val adapter: JsonAdapter<Info> = moshi.adapter()

        return adapter.fromJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromInfo(info: Info): String {
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<Info> = moshi.adapter()
        return adapter.toJson(info)
    }

    @TypeConverter
    @JvmStatic
    fun toStats(value :  String):  Stats? {
        val moshi = Moshi.Builder().add(BigDecimalAdapter).build()
        val adapter: JsonAdapter<Stats> = moshi.adapter()

        return adapter.fromJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromStats(stats: Stats): String {
        val moshi = Moshi.Builder().add(BigDecimalAdapter).build()
        val adapter: JsonAdapter<Stats> = moshi.adapter()
        return adapter.toJson(stats)
    }

    @TypeConverter
    @JvmStatic
    fun toImage(value :  String):  Image? {
        val moshi = Moshi.Builder().add(BigDecimalAdapter).build()
        val adapter: JsonAdapter<Image> = moshi.adapter()

        return adapter.fromJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromImage(stats: Image): String {
        val moshi = Moshi.Builder().add(BigDecimalAdapter).build()
        val adapter: JsonAdapter<Image> = moshi.adapter()
        return adapter.toJson(stats)
    }

    @TypeConverter
    @JvmStatic
    fun toSpells(value :  String):  List<Spell> {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(
            List::class.java,
            Spell::class.java
        )
        val adapter: JsonAdapter<List<Spell>> = moshi.adapter(type)

        return adapter.fromJson(value)?: emptyList()
    }

    @TypeConverter
    @JvmStatic
    fun fromSpells(list: List<Spell>): String {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(
            List::class.java,
            Spell::class.java
        )
        val adapter: JsonAdapter<List<Spell>> = moshi.adapter(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun toPassive(value :  String):  Passive? {
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<Passive> = moshi.adapter()

        return adapter.fromJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromPassive(stats: Passive): String {
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<Passive> = moshi.adapter()
        return adapter.toJson(stats)
    }
}