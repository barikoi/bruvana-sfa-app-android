package com.barikoi.cnlapp.Order_Create.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.R

class ConfirmOrderProductListAdapter(var mValues: List<Products>):RecyclerView.Adapter<ConfirmOrderProductListAdapter.ViewHolder>() {

    var productList: List<Products> = mValues

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_confirm_order_product_listview, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.productName.text = productList[position].product_name
        holder.addedProduct.text = productList[position].ordered_quantity.toString()
        holder.tvSubtoal.text = productList[position].ordered_total_price.toString()
        holder.tvUnitName.text = productList[position].unit_name
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView
        internal val addedProduct: TextView
        internal val tvSubtoal : TextView
        internal val tvUnitName : TextView

        init {
            productName = itemView.findViewById(R.id.tvProductName)
            addedProduct = itemView.findViewById(R.id.addedProductCount)
            tvSubtoal = itemView.findViewById(R.id.tvTotalPrice)
            tvUnitName = itemView.findViewById(R.id.tvUnitName)
        }
    }
}