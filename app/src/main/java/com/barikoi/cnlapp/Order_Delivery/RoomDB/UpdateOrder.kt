package com.barikoi.cnlapp.Order_Delivery.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UpdateOrder (
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "OutletId") var outletId: String,
    @ColumnInfo(name = "ItemCount") var itemsCount: Int,
    @ColumnInfo(name = "BounceCount") var bounceCount: Int,
    @ColumnInfo(name = "TotalPrice") var totalPrice: Double
    /*@TypeConverters(DataConvertor::class) var ProductsArray: ArrayList<Products>*/
        )