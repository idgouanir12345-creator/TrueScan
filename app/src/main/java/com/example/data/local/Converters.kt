package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.NutrientLevelsColorMap
import com.example.data.model.NutrimentDetails
import com.example.data.model.WatchlistIngredient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    @JvmStatic
    fun fromStringList(value: List<String>?): String {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(value: String?): List<String> {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return if (value.isNullOrEmpty()) emptyList() else adapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    @JvmStatic
    fun fromNutrientLevelsColorMap(value: NutrientLevelsColorMap?): String {
        val adapter = moshi.adapter(NutrientLevelsColorMap::class.java)
        return adapter.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toNutrientLevelsColorMap(value: String?): NutrientLevelsColorMap? {
        val adapter = moshi.adapter(NutrientLevelsColorMap::class.java)
        return if (value.isNullOrEmpty()) null else adapter.fromJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromNutrimentDetails(value: NutrimentDetails?): String {
        val adapter = moshi.adapter(NutrimentDetails::class.java)
        return adapter.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toNutrimentDetails(value: String?): NutrimentDetails? {
        val adapter = moshi.adapter(NutrimentDetails::class.java)
        return if (value.isNullOrEmpty()) null else adapter.fromJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromWatchlistIngredientList(value: List<WatchlistIngredient>?): String {
        val type = Types.newParameterizedType(List::class.java, WatchlistIngredient::class.java)
        val adapter = moshi.adapter<List<WatchlistIngredient>>(type)
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    @JvmStatic
    fun toWatchlistIngredientList(value: String?): List<WatchlistIngredient> {
        val type = Types.newParameterizedType(List::class.java, WatchlistIngredient::class.java)
        val adapter = moshi.adapter<List<WatchlistIngredient>>(type)
        return if (value.isNullOrEmpty()) emptyList() else adapter.fromJson(value) ?: emptyList()
    }
}
