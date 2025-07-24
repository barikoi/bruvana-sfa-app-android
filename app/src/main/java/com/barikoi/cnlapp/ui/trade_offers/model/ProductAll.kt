package com.barikoi.cnlapp.ui.trade_offers.model

import java.io.Serializable

class ProductAll(
    var productId: String,
    var productName: String,
    var unitName: String,
    var currentAvailableStock: String,
    var productPrice: String,
    var imageUrl: String,
    var tradeList: ArrayList<TradeProduct>
) : Serializable