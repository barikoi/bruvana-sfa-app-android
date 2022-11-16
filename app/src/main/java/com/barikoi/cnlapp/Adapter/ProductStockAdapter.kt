package com.barikoi.cnlapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.ProductStock
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.OutletStatistics
import com.barikoi.cnlapp.Utils.Api
import com.bumptech.glide.Glide

class ProductStockAdapter (val products: List<ProductStock>) : RecyclerView.Adapter<ProductStockAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductStockAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.product_view_stock, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProductStockAdapter.ViewHolder, position: Int) {
        val mItem = products[position]

        holder.productName.text = mItem.product_name
        holder.soldQuantity.text = mItem.sold_quantity
        holder.perUnitSold.text = mItem.per_unit_quantity+" "+mItem.unit_name

        if (!mItem.imageUrl.isNullOrEmpty() && !mItem.imageUrl.equals("null")){
            Glide.with(holder.itemView.context)
                .load(Api.base_url+mItem.imageUrl)
                .into(holder.imageProduct)
        }else{
            holder.imageProduct.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView
        internal val soldQuantity: TextView
        internal val perUnitSold: TextView
        internal val imageProduct: ImageView

        init {
            productName = itemView.findViewById(R.id.productName)
            soldQuantity = itemView.findViewById(R.id.tvSoldQuantity)
            imageProduct = itemView.findViewById(R.id.imageProduct)
            perUnitSold = itemView.findViewById(R.id.tvPerUnitSold)
        }
    }
}