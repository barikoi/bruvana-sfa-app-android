package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.OutletStatistics
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.bumptech.glide.Glide
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale


class OutletAdapter(val outlets: List<OutletStatistics>, var fromChoice: String) :
    RecyclerView.Adapter<OutletAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutletAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_outlet_statistics, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: OutletAdapter.ViewHolder, position: Int) {
        val item = outlets[position]
        holder.divider.visibility = View.VISIBLE
        if (!item.category.equals("null", true) && item.category.length > 0) {
            holder.tvCategory.visibility = View.VISIBLE
            holder.tvCategory.setText(item.category.get(0).toString().uppercase(Locale.ENGLISH))
        } else {
            holder.tvCategory.visibility = View.GONE
        }

        holder.shopName.setText(item.shop_name)
        if (!item.lastOrderDate.equals("null")) {
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yyyy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(item.lastOrderDate))
            holder.lastOrderDate.setText(holder.itemView.context.resources.getString(R.string.last_order_date) + orderDate)
        }
        if (fromChoice.equals("bounce")) {
            holder.btnDetails.text = holder.itemView.context.getString(R.string.bounce_item)
        } else {
            holder.btnDetails.text = holder.itemView.context.getString(R.string.details)
        }

        if (!item.shop_image.isNullOrEmpty() && !item.shop_image.equals("null")) {
            Glide.with(holder.itemView.context)
                .load(item.shop_image)
                .error(R.drawable.shop)
                .into(holder.imageShop)
        } else {
            //holder.imageProduct.visibility = View.INVISIBLE
        }

        holder.btnDetails.setOnClickListener {

            viewDialog(holder.itemView.context, item.shop_name, item.lastOrderDate, item.products)
        }

    }

    private fun viewDialog(
        mContext: Context,
        outlet_name: String,
        lastOrder: String,
        listItem: ArrayList<ProductStatistics>
    ) {
        val dialog = Dialog(mContext)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_product_list)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        val listView = dialog.findViewById<RecyclerView>(R.id.productList)
        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        var dformat = DecimalFormat("#.##")
        outletName.text = outlet_name
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val df = SimpleDateFormat("dd LLL yyyy", Locale.ENGLISH)
        val orderDate = df.format(oldDate.parse(lastOrder))
        tvLastOrderDate.text = mContext.resources.getString(R.string.last_order_date, orderDate)

        var itemCount = 0
        var grandTotal = 0.0
        if (listItem.size > 0) {
            for (i in 0 until listItem.size) {
                grandTotal = grandTotal + listItem[i].total_price
                itemCount = itemCount + listItem[i].quantity
            }

            val adapter = OutletProductAdapter(listItem)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        tvGrandTotal.text = dformat.format(grandTotal).toString()
        tvItemCount.text = itemCount.toString() + mContext.resources.getString(R.string.items)

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
        internal val imageShop: ImageView
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