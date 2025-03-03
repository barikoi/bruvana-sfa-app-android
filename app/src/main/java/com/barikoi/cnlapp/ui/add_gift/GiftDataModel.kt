package com.barikoi.cnlapp.ui.add_gift

import com.barikoi.cnlapp.data.remote.models.Gift

data class GiftDataModel(
    val id: Int,
    val name: String,
    val gift: List<Gift>
)
