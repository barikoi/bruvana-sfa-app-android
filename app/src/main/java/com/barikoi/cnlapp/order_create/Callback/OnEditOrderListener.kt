package com.barikoi.cnlapp.order_create.Callback

import com.barikoi.cnlapp.order_create.RoomDB.OrderList

interface OnEditOrderListener {
    fun onEdit(order: OrderList)
}