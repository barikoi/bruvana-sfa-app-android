package com.barikoi.cnlapp.Order_Create.Callback

import com.barikoi.cnlapp.Model.Products

interface OnValueChangeListener {

    fun onValueChanged(products: Products, position: Int)
}