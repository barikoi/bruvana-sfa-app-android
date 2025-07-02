package com.barikoi.cnlapp.order_create.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.databinding.ItemSelectShopBinding
import java.util.Locale

class AdapterSelectShop(
    private val onSelectShopListener: (Outlet) -> Unit
) : RecyclerView.Adapter<AdapterSelectShop.SelectShopViewHolder>(), Filterable {
    private var shopList: List<Outlet> = emptyList()
    var filterShopList: List<Outlet> = emptyList()

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
        holder.binding.tvShopName.text = filterShopList[position].outletName
        holder.binding.tvOwnerName.text = filterShopList[position].ownerName
        if (filterShopList[position].orderedToday == 1) {
            holder.binding.isOrderView.isVisible = true
            holder.binding.isOrderView.setImageResource(R.drawable.ic_ordered)
        } else if (filterShopList[position].isNoOrder == 1) {
            holder.binding.isOrderView.isVisible = true
            holder.binding.isOrderView.setImageResource(R.drawable.ic_no_ordered)
        } else {
            holder.binding.isOrderView.isVisible = false
        }

        holder.binding.btnDetails.setOnClickListener {
            onSelectShopListener(filterShopList[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newShopList: List<Outlet>) {
        this.shopList = newShopList
        this.filterShopList = newShopList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = filterShopList.size

    inner class SelectShopViewHolder(val binding: ItemSelectShopBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: ArrayList<Outlet> = ArrayList()

                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(shopList)
                } else {
                    val query = constraint.toString().trim().lowercase(Locale.ROOT)
                    shopList.forEach {
                        if (it.outletName.lowercase(Locale.ROOT).contains(query)) {
                            filteredList.add(it)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                if (results?.values is ArrayList<*>) {
                    filterShopList = emptyList()
                    filterShopList = results.values as List<Outlet>
                    notifyDataSetChanged()
                }
            }
        }
    }
}