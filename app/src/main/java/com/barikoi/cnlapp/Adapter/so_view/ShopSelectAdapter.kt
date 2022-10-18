package com.barikoi.cnlapp.Adapter.so_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Adapter.ShopListAdapter
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.OnSelectListener
import java.util.*

class ShopSelectAdapter(var mValues: List<Shops>, mListener: OnSelectListener): RecyclerView.Adapter<ShopSelectAdapter.ViewHolder>(),
    Filterable {

    var shopList: List<Shops> = mValues
    var mListener: OnSelectListener = mListener


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopSelectAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_shop_select_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ShopSelectAdapter.ViewHolder, position: Int) {
        holder.shopName.text= shopList[position].shop_name
        holder.ownerName.text = shopList[position].shop_owner

        holder.shopLayout.setOnClickListener {
            mListener.onShopSelected(shopList[position])
        }
    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                val filteredList: ArrayList<Shops> = ArrayList<Shops>()
                if (charString.isEmpty()) {
                    shopList = mValues
                } else {
                    //val filteredList: ArrayList<RetailShops> = ArrayList<RetailShops>()
                    for (row in shopList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.shop_name.toLowerCase()
                                .contains(charString.lowercase(Locale.getDefault())) || row.shop_name
                                .contains(charString)
                        ) {
                            filteredList.add(row)
                        }
                    }
                    //itemList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                shopList = results.values as List<Shops>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView
        internal val ownerName: TextView
        internal val shopLayout: LinearLayout
        init {
            shopName = itemView.findViewById(R.id.tvShopName)
            ownerName = itemView.findViewById(R.id.tvShopOwnerName)
            shopLayout = itemView.findViewById(R.id.shopLayout)

        }
    }
}