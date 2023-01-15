package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.OutletStatistics
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import java.text.DecimalFormat

class OutletProductAdapter(val products: List<ProductStatistics>) : RecyclerView.Adapter<OutletProductAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OutletProductAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_product_view, parent, false)
        return ViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: OutletProductAdapter.ViewHolder, position: Int) {
        val item = products[position]
        holder.productName.setText(item.product_name)
        holder.productType.setText(item.product_type)
        holder.perUnitPrice.setText(item.unit_price.toString())
        holder.tvCount.setText(item.quantity.toString())
        holder.tvCount.isEnabled = false
        var dformat = DecimalFormat("#.##")
        holder.subTotal.setText(dformat.format(item.total_price).toString())

        holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
        holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView
        internal val productType: TextView
        internal val perUnitPrice: TextView
        internal val tvCount: EditText
        internal val subTotal: TextView
        internal val btnMinus: ImageButton
        internal val btnAdd: ImageButton
        init {
            productName = itemView.findViewById(R.id.productName)
            productType = itemView.findViewById(R.id.tvProductVariation)
            perUnitPrice = itemView.findViewById(R.id.tvPerUnit)
            tvCount = itemView.findViewById(R.id.tvCount)
            subTotal = itemView.findViewById(R.id.tvSubTotal)
            btnMinus = itemView.findViewById(R.id.btnminus)
            btnAdd = itemView.findViewById(R.id.btnPlus)
        }
    }
}