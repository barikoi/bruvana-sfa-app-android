package com.barikoi.cnlapp.Order_Create.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SaveOrderDao {

    @Query("SELECT * FROM SaveOrder WHERE OutletId =:shopId")
    fun getOrdersDB(shopId: String): List<SaveOrder>?

    @Query("SELECT * FROM SaveOrder")
    fun getAllOrders(): List<SaveOrder>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg orders: SaveOrder?)

    @Query("DELETE FROM SaveOrder WHERE OutletId =:shopId")
    fun deleteByShop(shopId: String)

    @Query("DELETE FROM SaveOrder")
    fun deleteALL()

    @Query("UPDATE SaveOrder SET Items=:count, TotalPrice=:price WHERE OutletId =:shopId")
    fun update(shopId: String?, count: Int?, price: Double?)

}