package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import java.text.DecimalFormat

class OutletProductAdapter(val products: List<ProductStatistics>) : RecyclerView.Adapter<OutletProductAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_product_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = products[position]
        holder.productName.text = item.product_name
        holder.productType.text = item.unit_name
        holder.perUnitPrice.text = item.unit_price.toString()
        holder.tvCount.setText(item.quantity.toString())
        holder.tvCount.isEnabled = false
        val dFormat = DecimalFormat("#.##")
        holder.subTotal.text = dFormat.format(item.total_price).toString()

        holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
        holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView = itemView.findViewById(R.id.productName)
        internal val productType: TextView = itemView.findViewById(R.id.tvProductVariation)
        internal val perUnitPrice: TextView = itemView.findViewById(R.id.tvPerUnit)
        internal val tvCount: EditText = itemView.findViewById(R.id.tvCount)
        internal val subTotal: TextView = itemView.findViewById(R.id.tvSubTotal)
        internal val btnMinus: ImageButton = itemView.findViewById(R.id.btnminus)
        internal val btnAdd: ImageButton = itemView.findViewById(R.id.btnPlus)
    }
}