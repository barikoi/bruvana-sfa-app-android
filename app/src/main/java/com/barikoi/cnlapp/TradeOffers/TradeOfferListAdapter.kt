package com.barikoi.cnlapp.TradeOffers

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class TradeOfferListAdapter (val products: List<ProductAll>) : RecyclerView.Adapter<TradeOfferListAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TradeOfferListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_trade_offers_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: TradeOfferListAdapter.ViewHolder, position: Int) {
        val mItem = products[position]

        holder.productName.text = mItem.product_name
        holder.unitAvailable.text = mItem.current_available_stock+" "+mItem.unit_name

        if (!mItem.imageUrl.isNullOrEmpty() && !mItem.imageUrl.equals("null")){
            Glide.with(holder.itemView.context)
                .load(Api.base_url+mItem.imageUrl)
                .error(R.drawable.product)
                .into(holder.imageProduct)
        }else{
            //holder.imageProduct.visibility = View.INVISIBLE
        }

        if (mItem.tradeList.size>0){
            if (!mItem.tradeList[0].start_date.equals("null") && !mItem.tradeList[0].end_date.equals("null")){
                val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
                val startDate = df.format(oldDate.parse(mItem.tradeList[0].start_date))
                val endDate = df.format(oldDate.parse(mItem.tradeList[0].end_date))

                holder.tradeTimeline.visibility = View.VISIBLE
                holder.tradeTimeline.text = startDate+"-"+endDate
            }
            holder.previousPriceLayout.visibility = View.VISIBLE
            holder.previousPrice.text = mItem.product_price
            holder.currentPrice.text = mItem.tradeList[0].discounted_price
            holder.divider.visibility = View.VISIBLE
            //holder.tradeDivider.layoutParams.width = holder.previousPriceLayout.layoutParams.width
            /*val width1= holder.previousPrice.measuredWidth
            val width2 = holder.logoTaka.layoutParams.width
            val widthRange = width1+width2*/
            holder.previousPrice.getViewTreeObserver().addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    holder.previousPrice.setPaintFlags(holder.previousPrice.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
                    //holder.tradeDivider.layoutParams.width = holder.previousPrice.layoutParams.width+holder.logoTaka.layoutParams.width
                    holder.previousPrice.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                }
            })

        }else{
            holder.currentPrice.text = mItem.product_price
        }

    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView
        internal val currentPrice: TextView
        internal val previousPrice: TextView
        internal val tradeTimeline: TextView
        internal val divider: View
        internal val tradeDivider: View
        internal val unitAvailable: TextView
        internal val previousPriceLayout: RelativeLayout
        internal val imageProduct: ImageView
        internal val logoTaka: ImageView

        init {
            productName = itemView.findViewById(R.id.productName)
            currentPrice = itemView.findViewById(R.id.currentPrice)
            previousPrice = itemView.findViewById(R.id.previousPrice)
            tradeDivider = itemView.findViewById(R.id.tradeDivider)
            divider = itemView.findViewById(R.id.divider)
            tradeTimeline = itemView.findViewById(R.id.tradeTimeline)
            imageProduct = itemView.findViewById(R.id.imageProduct)
            unitAvailable = itemView.findViewById(R.id.unitCount)
            previousPriceLayout = itemView.findViewById(R.id.previousPriceLayout)
            logoTaka= itemView.findViewById(R.id.logoTaka)
        }
    }
}