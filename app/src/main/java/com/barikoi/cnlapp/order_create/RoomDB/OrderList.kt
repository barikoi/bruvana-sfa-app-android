package com.barikoi.cnlapp.order_create.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.barikoi.cnlapp.Model.Products
import java.io.Serializable

@Entity
data class OrderList(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "OrderId") var orderId: String,
    @ColumnInfo(name = "Ordered_At") var orderedAt: String,
    @ColumnInfo(name = "OrderStatus") var orderStatus: String,
    @ColumnInfo(name = "OutletId") var outletId: String,
    @ColumnInfo(name = "OutletName") var outletName: String,
    @ColumnInfo(name = "RouteId") var routeId: String,
    @ColumnInfo(name = "RouteName") var routeName: String,
    @ColumnInfo(name = "GrandTotal") var grandTotal: String,
    @ColumnInfo(name = "TotalQuantity") var totalQuantity: String,
    @ColumnInfo(name = "Latitude") var latitude: String,
    @ColumnInfo(name = "Longitude") var longitude: String,
    @ColumnInfo(name = "Distance") var distance: String,
    @TypeConverters(DataConvertor::class) var brands_array: ArrayList<Products>
) : Serializable