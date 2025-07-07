package com.barikoi.cnlapp.order_create.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.ProductStatistics
import com.barikoi.cnlapp.databinding.SingleProductViewBinding
import com.barikoi.cnlapp.utils.extension.format
import com.onesignal.common.AndroidSupportV4Compat

class OutletProductAdapter : RecyclerView.Adapter<OutletProductAdapter.OutletProductViewHolder>() {
    var products: List<ProductStatistics> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OutletProductViewHolder {

        return OutletProductViewHolder(
            SingleProductViewBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.single_product_view, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: OutletProductViewHolder, position: Int) {
        val item = products[position]
        holder.binding.productName.text = item.productName
        holder.binding.tvProductVariation.text = item.unitName
        holder.binding.tvPerUnit.text = item.unitPrice.toString()
        holder.binding.tvCount.setText(item.availableQuantity.toString())
        holder.binding.tvCount.isEnabled = false

        holder.binding.tvSubTotal.text = item.totalPrice.toString().format()

        holder.binding.btnPlus.drawable.setTint(
            AndroidSupportV4Compat.ContextCompat.getColor(
                holder.itemView.context,
                R.color.btn_gray_stroke
            )
        )
        holder.binding.btnminus.drawable.setTint(
            AndroidSupportV4Compat.ContextCompat.getColor(
                holder.itemView.context,
                R.color.btn_gray_stroke
            )
        )
    }

    override fun getItemCount(): Int = products.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateProducts(newProducts: List<ProductStatistics>) {
        products = newProducts
        notifyDataSetChanged()
    }

    inner class OutletProductViewHolder(val binding: SingleProductViewBinding) :
        RecyclerView.ViewHolder(binding.root)
}