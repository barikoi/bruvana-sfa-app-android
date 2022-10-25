package com.barikoi.cnlapp.callback

import com.barikoi.cnlapp.Model.Products

interface OnValueChangeListener {

    fun onValueChanged(products: Products, position: Int)
}