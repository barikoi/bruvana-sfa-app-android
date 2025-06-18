package com.barikoi.cnlapp.order_create.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.databinding.ItemSelectShopBinding

class AdapterSelectShop(
    private val onSelectShopListener: (Outlet) -> Unit
) : RecyclerView.Adapter<AdapterSelectShop.SelectShopViewHolder>() {
    private var shopList: List<Outlet> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectShopViewHolder {
        return SelectShopViewHolder(
            ItemSelectShopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false

            )
        )
    }

    override fun onBindViewHolder(
        holder: SelectShopViewHolder,
        position: Int
    ) {
        holder.binding.tvShopName.text = shopList[position].outletName
        holder.binding.tvOwnerName.text = shopList[position].ownerName
        if (shopList[position].orderedToday == 1) {
            holder.binding.isOrderView.isVisible = true
            holder.binding.isOrderView.setImageResource(R.drawable.ic_ordered)
        } else if (shopList[position].isNoOrder == 1) {
            holder.binding.isOrderView.isVisible = true
            holder.binding.isOrderView.setImageResource(R.drawable.ic_no_ordered)
        } else {
            holder.binding.isOrderView.isVisible = false
        }

        holder.binding.btnDetails.setOnClickListener {
            onSelectShopListener(shopList[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newShopList: List<Outlet>) {
        this.shopList = newShopList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = shopList.size

    inner class SelectShopViewHolder(val binding: ItemSelectShopBinding) :
        RecyclerView.ViewHolder(binding.root)
}