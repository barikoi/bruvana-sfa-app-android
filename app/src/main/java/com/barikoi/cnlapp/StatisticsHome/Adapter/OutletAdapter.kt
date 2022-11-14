package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.OutletStatistics
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OutletAdapter(val outlets: List<OutletStatistics>, var fromChoice : String) : RecyclerView.Adapter<OutletAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutletAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_outlet_satistics, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: OutletAdapter.ViewHolder, position: Int) {
        val item = outlets[position]
        holder.divider.visibility = View.VISIBLE
        if (!item.category.equals("null") && item.category.length> 0){
            holder.tvCategory.visibility = View.VISIBLE
            holder.tvCategory.setText(item.category)
        }else{
            holder.tvCategory.visibility = View.GONE
        }

        holder.shopName.setText(item.shop_name)
        if (!item.lastOrderDate.equals("null")){
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(item.lastOrderDate))
            holder.lastOrderDate.setText("Last Order Date: "+orderDate)
        }
        if (fromChoice.equals("bounce")){
            holder.btnDetails.setText("Bounce Item")
        }else{
            holder.btnDetails.setText("Details")
        }

        holder.btnDetails.setOnClickListener {

            viewDialog(holder.itemView.context, item.shop_name, holder.lastOrderDate.text.toString(), item.products)
        }

    }

    fun viewDialog(mContext: Context, outlet_name: String, lastOrder: String, listItem: ArrayList<ProductStatistics>){
        val dialog = Dialog(mContext)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.product_list_popup)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        val listView = dialog.findViewById<RecyclerView>(R.id.productList)
        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        var dformat = DecimalFormat("#.##")
        outletName.setText(outlet_name)
        tvLastOrderDate.setText(mContext.resources.getString(R.string.last_order_date)+ lastOrder)
        tvItemCount.setText(listItem.size.toString()+mContext.resources.getString(R.string.items))

        var grandTotal = 0.0
        if (listItem.size> 0){
            for (i in 0 until listItem.size){
                grandTotal = grandTotal+listItem[i].total_price
            }

            val adapter = OutletProductAdapter(listItem)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        tvGrandTotal.setText(dformat.format(grandTotal).toString())

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }

    override fun getItemCount(): Int {
        return outlets.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val tvCategory: TextView
        internal val shopName: TextView
        internal val lastOrderDate: TextView
        internal val imageShop:ImageView
        internal val divider: View
        internal val btnDetails: AppCompatButton

        init {
            tvCategory = itemView.findViewById(R.id.tvcategory)
            shopName = itemView.findViewById(R.id.shopName)
            imageShop = itemView.findViewById(R.id.imageShop)
            lastOrderDate = itemView.findViewById(R.id.orderDate)
            btnDetails = itemView.findViewById(R.id.btnDetails)
            divider = itemView.findViewById(R.id.divider)
        }
    }

}