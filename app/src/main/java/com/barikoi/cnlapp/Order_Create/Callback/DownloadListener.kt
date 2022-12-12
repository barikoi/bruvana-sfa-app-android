package com.barikoi.cnlapp.Order_Create.Callback

import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList

interface DownloadListener {
    fun onClicked(order: OrderList)
}