package com.barikoi.cnlapp.RoomDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OrderListDao {

    @Query("SELECT * FROM OrderList")
    fun getOrdersDB(id: Int?): List<OrderList>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg orders: OrderList?)
}