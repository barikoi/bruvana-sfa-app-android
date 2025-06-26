package com.barikoi.cnlapp.ui.add_gift.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.data.remote.models.Gift
import com.barikoi.cnlapp.data.remote.models.GiftModel
import com.barikoi.cnlapp.databinding.ItemGiftMainBinding

class AdapterMainGift(
    private val addClickListener: (Int, Int) -> Unit,
    private val incrementClickListener: (Int, Int) -> Unit,
    private val decrementClickListener: (Int, Int) -> Unit,
    private val removeItemClickListener: (Int, Int) -> Unit
) : Adapter<AdapterMainGift.GiftViewHolder>() {

    private var giftList: List<GiftModel> = emptyList()

    private lateinit var adapterGift: AdapterGift

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

    fun updateByPos(pos: Int, list: List<Gift>) {
        adapterGift.updateByPos(pos, list)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.binding.tvTitle.text = giftList[position].title

        adapterGift = AdapterGift(
            addClickListener = {
                addClickListener(position, it)
            },
            incrementClickListener = {
                incrementClickListener(position, it)
            },
            decrementClickListener = {
                decrementClickListener(position, it)
            },

            removeItemClickListener = {
                removeItemClickListener(position, it)
            }
        )


        holder.binding.rcvGiftMain.layoutManager =
            GridLayoutManager(holder.itemView.context, 1, GridLayoutManager.HORIZONTAL, false)
        holder.binding.rcvGiftMain.adapter = adapterGift
        adapterGift.updateList(giftList[position].gifts)
    }
}