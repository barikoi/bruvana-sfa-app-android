package com.barikoi.cnlapp.order_create.combo

import androidx.recyclerview.widget.DiffUtil
import com.barikoi.cnlapp.data.remote.models.offer.Offer

class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
    override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean = oldItem == newItem
}