package com.barikoi.cnlapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import java.util.*

class ShopListAdapter(var mValues: List<Shops>): RecyclerView.Adapter<ShopListAdapter.ViewHolder>(),
    Filterable {

    var shopList: List<Shops> = mValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_shop_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ShopListAdapter.ViewHolder, position: Int) {
        holder.shopName.text = shopList[position].shop_name
        holder.address.text = shopList[position].address
        holder.shopState.text = shopList[position].state
        holder.shopCode.text = shopList[position].shop_code
        holder.shopType.text = shopList[position].shop_type
        holder.distributorName.text = shopList[position].distributor_office
        holder.territoryName.text = shopList[position].territory_name

    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView
        internal val shopState: TextView
        internal val address: TextView
        internal val shopCode: TextView
        internal val distributorName: TextView
        internal val shopType: TextView
        internal val territoryName: TextView
        init {
            shopName = itemView.findViewById(R.id.shop_name)
            shopState = itemView.findViewById(R.id.shop_state)
            territoryName = itemView.findViewById(R.id.territory_name)
            address = itemView.findViewById(R.id.address)
            shopCode = itemView.findViewById(R.id.shop_code)
            shopType = itemView.findViewById(R.id.shop_type)
            distributorName = itemView.findViewById(R.id.distributor_name)

        }
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
                                .contains(charString.lowercase(Locale.getDefault())) || row.shop_code
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
}