package com.barikoi.cnlapp.Adapter.so_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.OrderList
import com.barikoi.cnlapp.callback.OnEditOrderListener

class ConfirmOrderListAdapter(mValues: List<OrderList>, var mListener: OnEditOrderListener): RecyclerView.Adapter<ConfirmOrderListAdapter.ViewHolder>() {
    var orderList: List<OrderList> = mValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_confirm_order_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.shopName.text = orderList[position].outletName
        holder.subTotal.text = orderList[position].grandTotal

        if (orderList[position].brands_array.size > 0){
            val adapter = ConfirmOrderProductListAdapter(orderList[position].brands_array)
            holder.productList.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        holder.addMore.setOnClickListener {
            mListener.onEdit(orderList[position])
        }

        holder.editItem.setOnClickListener {
            mListener.onEdit(orderList[position])
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView
        internal val subTotal: TextView
        internal val addMore: TextView
        internal val editItem: ImageView
        internal val productList: RecyclerView

        init {
            shopName = itemView.findViewById(R.id.tvShopName)
            subTotal = itemView.findViewById(R.id.tvSubTotal)
            productList = itemView.findViewById(R.id.productlist)
            addMore = itemView.findViewById(R.id.tvAddMore)
            editItem = itemView.findViewById(R.id.btn_edit)

        }
    }


}