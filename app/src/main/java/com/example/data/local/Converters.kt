package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DataQualityStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val intListType = Types.newParameterizedType(List::class.java, Int::class.javaObjectType)

    private val stringListAdapter = moshi.adapter<List<String>>(listType)
    private val intListAdapter = moshi.adapter<List<Int>>(intListType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return stringListAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            stringListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return intListAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            intListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromDataQualityStatus(status: DataQualityStatus): String {
        return status.name
    }

    @TypeConverter
    fun toDataQualityStatus(value: String?): DataQualityStatus {
        return try {
            DataQualityStatus.valueOf(value ?: DataQualityStatus.VERIFIED.name)
        } catch (e: Exception) {
            DataQualityStatus.VERIFIED
        }
    }
}
