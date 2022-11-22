package com.barikoi.cnlapp.TradeOffers.Model

import java.io.Serializable

class ProductAll (
    var product_id: String,
    var product_name: String,
    var unit_name: String,
    var current_available_stock: String,
    var product_price: String,
    var imageUrl: String,
    var tradeList: ArrayList<TradeProduct>

): Serializable