package com.barikoi.cnlapp.Adapter.so_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.OrderList

class ConfirmOrderList(var mValues: List<OrderList>): RecyclerView.Adapter<ConfirmOrderList.ViewHolder>() {
    var orderList: List<OrderList> = mValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_confirm_order_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.shopName.text = orderList[position].outletName
        holder.subTotal.text = orderList[position].grandTotal

        if (orderList[position].brands_array.size > 0){
            val adapter = ConfirmOrderProductList(orderList[position].brands_array)
            holder.productList.adapter = adapter
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView
        internal val subTotal: TextView
        internal val productList: RecyclerView

        init {
            shopName = itemView.findViewById(R.id.tvShopName)
            subTotal = itemView.findViewById(R.id.tvSubTotal)
            productList = itemView.findViewById(R.id.productlist)

        }
    }


}