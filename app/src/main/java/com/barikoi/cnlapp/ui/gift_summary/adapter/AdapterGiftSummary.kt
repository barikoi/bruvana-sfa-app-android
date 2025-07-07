package com.barikoi.cnlapp.ui.gift_summary.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.GiftSummary
import com.barikoi.cnlapp.databinding.ItemGiftSummaryBinding
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterGiftSummary : RecyclerView.Adapter<AdapterGiftSummary.GiftSummaryViewHolder>() {
    private var giftSummaryList: List<GiftSummary> =
        emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GiftSummaryViewHolder {
        return GiftSummaryViewHolder(
            ItemGiftSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: GiftSummaryViewHolder,
        position: Int
    ) {
        val giftSummary = giftSummaryList[position]

        holder.binding.tvGiftTitle.text = giftSummary.name
        holder.binding.tvTotalCount.text = giftSummary.total.toString()

        if (giftSummary.isExpanded) {
            holder.binding.rcvShopTypeGift.visibility = View.VISIBLE
            holder.binding.ivExpend.setImageResource(R.drawable.ic__arrow_up)

            val adapter = AdapterGiftShopType()
            holder.binding.rcvShopTypeGift.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(
                    holder.binding.root.context
                )
            holder.binding.rcvShopTypeGift.setHasFixedSize(true)
            holder.binding.rcvShopTypeGift.adapter = adapter
            adapter.setGiftShopTypeList(giftSummary.giftData)


        } else {
            holder.binding.rcvShopTypeGift.visibility = View.GONE
            holder.binding.ivExpend.setImageResource(R.drawable.ic_arrow_down)
        }

        holder.binding.ivExpend.setHapticClickListener {
            giftSummary.isExpanded = !giftSummary.isExpanded
            notifyItemChanged(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setGiftSummaryList(giftSummaryList: List<GiftSummary>) {
        val updatedList = giftSummaryList.mapIndexed { _, item ->
            item.copy(isExpanded = true)
        }
        this.giftSummaryList = updatedList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = giftSummaryList.size

    inner class GiftSummaryViewHolder(val binding: ItemGiftSummaryBinding) :
        RecyclerView.ViewHolder(binding.root)
}