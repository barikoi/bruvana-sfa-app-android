package com.barikoi.cnlapp.ui.add_gift

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.databinding.ItemGiftMainBinding

class AdapterMainGift(
    private val addClickListener: (Int, Int) -> Unit,
    private val incrementClickListener: (Int) -> Unit,
    private val decrementClickListener: (Int) -> Unit,
    private val removeItemClickListener: (Int) -> Unit
) : Adapter<AdapterMainGift.GiftViewHolder>() {


    private var giftList: List<GiftModel> = emptyList()

    private var adapterImage: AdapterImagePickerView = AdapterImagePickerView {

    }

    inner class GiftViewHolder(val binding: ItemGiftMainBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        return GiftViewHolder(
            ItemGiftMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = giftList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<GiftModel>) {
        giftList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.binding.tvTitle.text = giftList[position].title

        val adapterGift = AdapterGift(
            addClickListener = {
                addClickListener(position, it)
            },
            incrementClickListener,
            decrementClickListener,
            removeItemClickListener
        )


        holder.binding.rcvGiftMain.layoutManager =
            GridLayoutManager(holder.itemView.context, 1, GridLayoutManager.HORIZONTAL, false)
        holder.binding.rcvGiftMain.adapter = adapterGift
        adapterGift.updateList(giftList[position].gifts)

    }
}