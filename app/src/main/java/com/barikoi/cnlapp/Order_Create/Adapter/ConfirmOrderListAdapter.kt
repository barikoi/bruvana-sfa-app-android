package com.barikoi.cnlapp.Order_Create.Adapter

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
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ConfirmOrderListAdapter(var mValues: List<OrderList>, var mListener: OnEditOrderListener, var from: String): RecyclerView.Adapter<ConfirmOrderListAdapter.ViewHolder>(), Filterable {
    var orderList: List<OrderList> = mValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_confirm_order_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var dformat = DecimalFormat("#.##")
        holder.shopName.text = orderList[position].outletName
        holder.subTotal.text = dformat.format(orderList[position].grandTotal.toDouble()).toString()

        if (orderList[position].brands_array.size > 0){
            val adapter = ConfirmOrderProductListAdapter(orderList[position].brands_array)
            holder.productList.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        if (!orderList[position].orderedAt.equals("null")){
            holder.orderAt.visibility = View.VISIBLE
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(orderList[position].orderedAt))
            holder.orderAt.setText(holder.itemView.context.resources.getString(R.string.ordered_at)+orderDate)
        }else{
            holder.orderAt.visibility = View.GONE
        }

        holder.addMore.setOnClickListener {
            mListener.onEdit(orderList[position])
        }

        holder.editItem.setOnClickListener {
            mListener.onEdit(orderList[position])
        }

        if (from.equals("summary")){
            holder.editItem.visibility = View.GONE
            holder.downloadChalan.visibility = View.GONE
            holder.addMore.visibility = View.INVISIBLE
        }

        holder.downloadChalan.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                val filteredList: ArrayList<OrderList> = ArrayList()
                if (charString.isEmpty()) {
                    orderList = mValues
                } else {
                    //val filteredList: ArrayList<RetailShops> = ArrayList<RetailShops>()
                    for (row in orderList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.outletName.toLowerCase().contains(charString.lowercase(Locale.getDefault()))) {
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
                orderList = results.values as List<OrderList>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView
        internal val orderAt: TextView
        internal val subTotal: TextView
        internal val addMore: TextView
        internal val editItem: ImageView
        internal val downloadChalan: ImageView
        internal val productList: RecyclerView

        init {
            shopName = itemView.findViewById(R.id.tvShopName)
            orderAt = itemView.findViewById(R.id.tvOrderDate)
            subTotal = itemView.findViewById(R.id.tvSubTotal)
            productList = itemView.findViewById(R.id.productlist)
            addMore = itemView.findViewById(R.id.tvAddMore)
            editItem = itemView.findViewById(R.id.btn_edit)
            downloadChalan = itemView.findViewById(R.id.btn_download)

        }
    }


}