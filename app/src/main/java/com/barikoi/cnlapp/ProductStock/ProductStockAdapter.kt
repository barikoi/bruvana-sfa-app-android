package com.barikoi.cnlapp.ProductStock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.barikoi.cnlapp.R
import com.bumptech.glide.Glide


class ProductStockAdapter (val products: List<ProductStock>) : RecyclerView.Adapter<ProductStockAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.product_view_stock, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val mItem = products[position]

        holder.productName.text = mItem.product_name
        holder.soldQuantity.text = mItem.sold_quantity
        if (!mItem.unit_name.equals("null")) {
            holder.perUnitSold.text = mItem.per_unit_quantity + " " + mItem.unit_name
        }else{
            holder.perUnitSold.text = mItem.per_unit_quantity + " "
        }

        // create a ProgressDrawable object which we will show as placeholder
        val drawable = CircularProgressDrawable(holder.itemView.context)
        drawable.setColorSchemeColors(
            holder.itemView.context.resources.getColor(R.color.cnl_color_1),
            holder.itemView.context.resources.getColor(R.color.cnl_color_2)
        )
        drawable.centerRadius = 20f
        drawable.strokeWidth = 6f
        drawable.start()

        if (!mItem.imageUrl.isNullOrEmpty() && !mItem.imageUrl.equals("null")){
            Glide.with(holder.itemView.context)
                .load(mItem.imageUrl)
                .placeholder(drawable)
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