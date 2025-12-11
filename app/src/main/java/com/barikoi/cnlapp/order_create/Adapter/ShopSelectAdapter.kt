package com.barikoi.cnlapp.order_create.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.SingleOutletStatisticsBinding
import com.barikoi.cnlapp.order_create.Callback.DialogListener
import com.barikoi.cnlapp.order_create.Callback.OnSelectListener
import com.barikoi.cnlapp.utils.ViewUtils
import java.text.SimpleDateFormat
import java.util.Locale

class ShopSelectAdapter(val mValues: List<Shops>, val mListener: OnSelectListener) :
    RecyclerView.Adapter<ShopSelectAdapter.ViewHolder>(),
    Filterable {

    var shopList: List<Shops> = mValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SingleOutletStatisticsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.background = holder.itemView.resources.getDrawable(R.drawable.cardview_bg)
        holder.binding.btnDetails.text = holder.itemView.resources.getString(R.string.select)
        holder.binding.ownerName.visibility = View.VISIBLE

        holder.setIsRecyclable(false)

        holder.binding.shopName.text = shopList[position].shop_name
        holder.binding.ownerName.text = shopList[position].shop_owner
        if (!shopList[position].lastOrderDate.equals("null")) {
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yyyy", Locale.ENGLISH)
            val orderDate = oldDate.parse(shopList[position].lastOrderDate)?.let { df.format(it) }
            holder.binding.orderDate.text =
                holder.itemView.context.resources.getString(R.string.last_order_date, orderDate)
        }
        if (shopList[position].category.isNotEmpty() && !shopList[position].category.equals(
                "null",
                true
            )
        ) {
            holder.binding.tvCategory.text =
                shopList[position].category.get(0).toString().uppercase(Locale.getDefault())
        }

        if (shopList[position].isOrdered == 1) {
            holder.binding.isOrderView.setImageResource(R.drawable.ic_ordered)
            holder.binding.isOrderView.visibility = View.VISIBLE
        } else if (shopList[position].isNoOrdered == 1) {
            holder.binding.isOrderView.setImageResource(R.drawable.ic_no_ordered)
            holder.binding.isOrderView.visibility = View.VISIBLE
        } else {
            holder.binding.isOrderView.visibility = View.GONE
        }

        holder.binding.btnDetails.setOnClickListener {
            if (shopList[position].isOrdered == 1 || shopList[position].isNoOrdered == 1) {
                ViewUtils.viewDialogResponse(
                    holder.itemView.context,
                    "Already visited this outlet for today",
                    object : DialogListener {
                        override fun onConfirmed() {}

                        override fun onCanceled() {}
                    })
            } else {
                mListener.onShopSelected(shopList[position])
            }

        }
    }

    override fun getItemCount(): Int = shopList.size


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                val filteredList: ArrayList<Shops> = ArrayList()
                if (charString.isEmpty()) {
                    shopList = mValues
                } else {
                    for (row in mValues) {
                        if (row.shop_name.lowercase(Locale.getDefault())
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

    inner class ViewHolder(val binding: SingleOutletStatisticsBinding) :
        RecyclerView.ViewHolder(binding.root)
}