package com.labpro.nimons360.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<String>>(
                value,
                object : TypeToken<List<String>>() {}.type,
            )
        }.getOrDefault(emptyList())
    }
}
