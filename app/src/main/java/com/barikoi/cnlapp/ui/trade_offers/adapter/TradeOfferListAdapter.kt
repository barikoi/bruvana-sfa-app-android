package com.barikoi.cnlapp.ui.trade_offers.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.ui.trade_offers.model.ProductAll
import com.barikoi.cnlapp.utils.extension.formateDate
import java.text.SimpleDateFormat
import java.util.Locale

class TradeOfferListAdapter(mValues: List<ProductAll>) :
    RecyclerView.Adapter<TradeOfferListAdapter.ViewHolder>() {
    var products: List<ProductAll> = mValues
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_trade_offers_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mItem = products[position]

        holder.productName.text = mItem.productName
        holder.unitAvailable.text = mItem.currentAvailableStock + " " + mItem.unitName

        if (mItem.imageUrl.isNotEmpty() && mItem.imageUrl != "null") {
            holder.imageProduct.load(
                mItem.imageUrl,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.product)
                }
            )
        } else {
            //holder.imageProduct.visibility = View.INVISIBLE
        }

        if (mItem.tradeList.isNotEmpty()) {
            if (mItem.tradeList[0].startDate != "null" && mItem.tradeList[0].endDate != "null") {
                val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
                val startDate = mItem.tradeList[0].startDate.formateDate()
                val endDate = mItem.tradeList[0].endDate.formateDate()

                holder.tradeTimeline.visibility = View.VISIBLE
                holder.tradeTimeline.text =
                    holder.tradeTimeline.context.getString(R.string.date_range_, startDate, endDate)
            }
            holder.previousPriceLayout.visibility = View.VISIBLE
            holder.previousPrice.text = mItem.productPrice
            holder.currentPrice.text = mItem.tradeList[0].discountedPrice
            holder.divider.visibility = View.VISIBLE
            holder.previousPrice.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    holder.previousPrice.paintFlags =
                        holder.previousPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    holder.previousPrice.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

        } else {
            holder.currentPrice.text = mItem.productPrice
        }

    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView = itemView.findViewById(R.id.productName)
        internal val currentPrice: TextView = itemView.findViewById(R.id.currentPrice)
        internal val previousPrice: TextView = itemView.findViewById(R.id.previousPrice)
        internal val tradeTimeline: TextView = itemView.findViewById(R.id.tradeTimeline)
        internal val divider: View = itemView.findViewById(R.id.divider)
        internal val tradeDivider: View = itemView.findViewById(R.id.tradeDivider)
        internal val unitAvailable: TextView = itemView.findViewById(R.id.unitCount)
        internal val previousPriceLayout: RelativeLayout =
            itemView.findViewById(R.id.previousPriceLayout)
        internal val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        internal val logoTaka: ImageView = itemView.findViewById(R.id.logoTaka)
    }
}