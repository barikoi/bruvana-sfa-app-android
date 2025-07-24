package com.barikoi.cnlapp.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.OnEditShopListener
import java.util.Locale

class ShopListAdapter(var mValues: List<Shops>, var mListener: OnEditShopListener) :
    RecyclerView.Adapter<ShopListAdapter.ViewHolder>(),
    Filterable {

    var shopList: List<Shops> = mValues
    lateinit var mRecyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.single_shop_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)

        holder.shopName.text = shopList[position].shop_name
        holder.address.text = shopList[position].address
        if (shopList[position].state.equals("active", true)) {
            holder.shopState.visibility = View.VISIBLE
        } else {
            holder.shopState.visibility = View.GONE
        }
        if (!shopList[position].shop_id.equals("null")) holder.shopCode.text =
            shopList[position].shop_id

        holder.shopType.text = shopList[position].shop_type
        holder.territoryName.text = shopList[position].territory_name

        if (shopList[position].isVerified == 0) {
            holder.imageNewTag.visibility = View.VISIBLE
        } else {
            holder.imageNewTag.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener {
            mListener.onEdit(shopList[position])
        }
    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView = itemView.findViewById(R.id.shop_name)
        internal val shopState: ImageView = itemView.findViewById(R.id.shop_state)
        internal val address: TextView = itemView.findViewById(R.id.address)
        internal val shopCode: TextView = itemView.findViewById(R.id.shop_code)
        internal val distributorName: TextView = itemView.findViewById(R.id.distributor_name)
        internal val shopType: TextView = itemView.findViewById(R.id.shop_type)
        internal val shopCategory: TextView = itemView.findViewById(R.id.shop_category)
        internal val territoryName: TextView = itemView.findViewById(R.id.territory_name)
        internal val imageShop: ImageView = itemView.findViewById(R.id.imageShop)
        internal val imageNewTag: ImageView = itemView.findViewById(R.id.imgNewTag)
        internal val btnEdit: ImageView = itemView.findViewById(R.id.btn_edit)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                val filteredList: ArrayList<Shops> = ArrayList<Shops>()
                if (charString.isEmpty()) {
                    shopList = mValues
                } else {
                    for (row in mValues) {
                        if (row.shop_name.lowercase(Locale.ROOT)
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(row)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                shopList = results.values as List<Shops>
                notifyDataSetChanged()
            }
        }
    }
}