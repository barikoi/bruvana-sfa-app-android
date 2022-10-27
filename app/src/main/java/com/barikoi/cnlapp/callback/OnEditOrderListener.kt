package com.barikoi.cnlapp.callback

import com.barikoi.cnlapp.RoomDb.OrderList

interface OnEditOrderListener {
    fun onEdit(order: OrderList)
}