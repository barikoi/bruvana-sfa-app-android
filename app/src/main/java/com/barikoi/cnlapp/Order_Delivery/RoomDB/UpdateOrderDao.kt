package com.barikoi.cnlapp.Order_Delivery.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UpdateOrderDao {

    @Query("SELECT * FROM UpdateOrder WHERE OutletId =:shopId")
    fun getOrdersDB(shopId: String): List<UpdateOrder>?

    @Query("SELECT * FROM UpdateOrder")
    fun getAllOrders(): List<UpdateOrder>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg orders: UpdateOrder)

    @Query("DELETE FROM UpdateOrder WHERE OutletId =:shopId")
    fun deleteByShop(shopId: String)

    @Query("DELETE FROM UpdateOrder")
    fun deleteALL()

    @Query("UPDATE UpdateOrder SET ItemCount=:count, TotalPrice=:price, BounceCount=:bounce WHERE OutletId =:shopId")
    fun update(shopId: String?, count: Int?,bounce: Int?, price: Double?)

}