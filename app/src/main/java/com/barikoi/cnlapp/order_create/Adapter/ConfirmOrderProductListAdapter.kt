package com.barikoi.cnlapp.order_create.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.utils.extension.format

class ConfirmOrderProductListAdapter(mValues: List<Products>) :
    RecyclerView.Adapter<ConfirmOrderProductListAdapter.ViewHolder>() {

    var productList: List<Products> = mValues

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_confirm_order_product_listview, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productName.text = productList[position].productName
        holder.tvOffer.isVisible = !productList[position].offerID.isNullOrEmpty()
        holder.addedProduct.text = productList[position].orderedQuantity.toString()
        holder.tvSubTotal.text =
            if (productList[position].offerID.isNullOrEmpty()) {
                productList[position].orderedTotalPrice.toString().format()
            } else {
                (productList[position].orderedTotalPrice * productList[position].orderedQuantity).toString()
                    .format()
            }

        holder.tvUnitName.text = productList[position].unitName
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView = itemView.findViewById(R.id.tvProductName)
        internal val addedProduct: TextView = itemView.findViewById(R.id.addedProductCount)
        internal val tvSubTotal: TextView = itemView.findViewById(R.id.tvTotalPrice)
        internal val tvUnitName: TextView = itemView.findViewById(R.id.tvUnitName)
        internal val tvOffer: TextView = itemView.findViewById(R.id.tvOffer)
    }
}