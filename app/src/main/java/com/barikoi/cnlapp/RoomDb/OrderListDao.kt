package com.barikoi.cnlapp.RoomDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OrderListDao {

    @Query("SELECT * FROM OrderList WHERE OutletId =:shopId")
    fun getOrdersDB(shopId: String): List<OrderList>?

    @Query("SELECT * FROM OrderList")
    fun getAllOrders(): List<OrderList>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg orders: OrderList?)

    @Query("DELETE FROM OrderList WHERE OutletId =:shopId")
    fun deleteByShop(shopId: String)
}