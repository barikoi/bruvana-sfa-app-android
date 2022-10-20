package com.barikoi.cnlapp.RoomDb

import androidx.room.TypeConverter
import com.barikoi.cnlapp.Model.Products
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

class DataConvertor {
    @TypeConverter
    fun fromString(value: String?): ArrayList<Products>? {
        val listType: Type = object : TypeToken<ArrayList<Products>?>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Products>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

}