package com.barikoi.cnlapp.ui.add_gift

import com.barikoi.cnlapp.data.remote.models.Gift

data class GiftModel(
    val title: String,
    val gifts: List<Gift>
)
