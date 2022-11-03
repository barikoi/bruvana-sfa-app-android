package com.barikoi.cnlapp.Order_Create.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SaveOrder (
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "OutletId") var outletId: String,
    @ColumnInfo(name = "Items") var itemsCount: Int,
    @ColumnInfo(name = "TotalPrice") var totalPrice: Double
    /*@TypeConverters(DataConvertor::class) var ProductsArray: ArrayList<Products>*/
        )