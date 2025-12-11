package com.barikoi.cnlapp.ui.gift_summary.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.data.remote.models.GiftData
import com.barikoi.cnlapp.databinding.ItemShopTypeGiftBinding

class AdapterGiftShopType : RecyclerView.Adapter<AdapterGiftShopType.GiftShopTypeViewHolder>() {
    private var giftShopTypeList: List<GiftData> =
        emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GiftShopTypeViewHolder {
        return GiftShopTypeViewHolder(
            ItemShopTypeGiftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: GiftShopTypeViewHolder,
        position: Int
    ) {
        val giftSummaryShopType = giftShopTypeList[position]
        holder.binding.tvName.text = giftSummaryShopType.name
        holder.binding.tvTotal.text = giftSummaryShopType.value.toString()

    }
    @SuppressLint("NotifyDataSetChanged")
    fun setGiftShopTypeList(giftShopTypeList: List<GiftData>) {
        this.giftShopTypeList = giftShopTypeList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = giftShopTypeList.size

    inner class GiftShopTypeViewHolder(val binding: ItemShopTypeGiftBinding) :
        RecyclerView.ViewHolder(binding.root)
}