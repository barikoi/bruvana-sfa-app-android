package com.barikoi.cnlapp.RoomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.barikoi.cnlapp.Model.Products

@Entity
data class SaveOrder (
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "OutletId") var outletId: String,
    @ColumnInfo(name = "Items") var itemsCount: Int,
    @ColumnInfo(name = "TotalPrice") var totalPrice: Double,
    /*@TypeConverters(DataConvertor::class) var ProductsArray: ArrayList<Products>*/
        )