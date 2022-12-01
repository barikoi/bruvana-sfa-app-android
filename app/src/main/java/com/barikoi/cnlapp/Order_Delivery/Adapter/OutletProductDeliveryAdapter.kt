package com.barikoi.cnlapp.Order_Delivery.Adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Order_Create.Callback.OnValueChangeListener
import com.barikoi.cnlapp.Order_Create.RoomDB.SaveOrder
import com.barikoi.cnlapp.Order_Delivery.RoomDB.UpdateOrder
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.Utils.Api
import java.text.DecimalFormat

class OutletProductDeliveryAdapter(val products: List<ProductStatistics>, var mListener: OnValueChangeListener, var outletId: String, var from: String) : RecyclerView.Adapter<OutletProductDeliveryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OutletProductDeliveryAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_product_view, parent, false)
        return ViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: OutletProductDeliveryAdapter.ViewHolder, position: Int) {
        val item = products[position]
        val appDatabase = AppDatabase.getInstance(holder.itemView.context)
        holder.productName.setText(item.product_name)
        holder.productType.setText(item.product_type)
        holder.perUnitPrice.setText(item.unit_price.toString())
        holder.tvCount.setText(item.quantity.toString())
        holder.tvCount.isEnabled = false
        var dformat = DecimalFormat("#.##")
        holder.subTotal.setText(dformat.format(item.total_price).toString())
        if (item.quantity > 0) {
            if (from.equals("PENDING", true)) {
                holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.cnl_color_2))
            }
        }else{
            holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
        }
        holder.btnAdd.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
        holder.productCount.isEnabled = false

        holder.btnMinus.setOnClickListener {
            val qtyValue = holder.productCount.text.toString().toInt() - 1
            holder.productCount.setText(qtyValue.toString())
            val subtotal = item.unit_price * holder.productCount.text.toString().toInt()
            holder.subTotal.setText(dformat.format(subtotal).toString())
            item.bounced_quantity = item.bounced_quantity+1
            item.quantity = holder.productCount.text.toString().toInt()
            item.total_price = dformat.format(subtotal).toDouble()
            val prodList = appDatabase!!.updateOrderDao().getOrdersDB(outletId)
            if (prodList!!.size > 0){
                Log.d("Product", "item count minus: "+prodList[0].itemsCount+ " shopId: "+prodList[0].outletId)
                appDatabase.updateOrderDao().update(
                    outletId,
                    prodList[0].itemsCount-1,
                    item.bounced_quantity,
                    prodList[0].totalPrice-item.unit_price
                )
            }else{
                appDatabase.updateOrderDao().insertAll(
                    UpdateOrder(
                        null,
                        outletId,
                        holder.productCount.text.toString().toInt(),
                        item.bounced_quantity,
                        holder.subTotal.text.toString().toDouble()
                    )
                )
            }

            mListener.onValueChanged(item, position)
        }
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
        internal val btnMinus : ImageButton
        internal val btnAdd : ImageButton
        internal val productCount: EditText
        init {
            productName = itemView.findViewById(R.id.productName)
            productType = itemView.findViewById(R.id.tvProductVariation)
            perUnitPrice = itemView.findViewById(R.id.tvPerUnit)
            tvCount = itemView.findViewById(R.id.tvCount)
            subTotal = itemView.findViewById(R.id.tvSubTotal)
            btnMinus = itemView.findViewById(R.id.btnminus)
            btnAdd = itemView.findViewById(R.id.btnPlus)
            productCount = itemView.findViewById(R.id.tvCount)
        }
    }
}