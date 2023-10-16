package com.barikoi.cnlapp.Order_Create.Adapter

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ConfirmOrderListAdapter(var mValues: List<OrderList>, var mListener: OnEditOrderListener, var from: String): RecyclerView.Adapter<ConfirmOrderListAdapter.ViewHolder>(), Filterable {
    var orderList: List<OrderList> = mValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_confirm_order_view, parent, false)
        return ViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val queue = RequestQueueSingleton.getInstance(holder.itemView.context).getRequestQueue()
        var dformat = DecimalFormat("#.##")
        holder.routeName.text = orderList[position].routeName
        holder.shopName.text = orderList[position].outletName
        holder.subTotal.text = dformat.format(orderList[position].grandTotal.toDouble()).toString()

        if (orderList[position].brands_array.size > 0){
            val adapter = ConfirmOrderProductListAdapter(orderList[position].brands_array)
            holder.productList.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        if (!orderList[position].orderedAt.equals("null")){
            holder.orderAt.visibility = View.VISIBLE
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(orderList[position].orderedAt))
            val builder = SpannableStringBuilder()
            val str1 = SpannableString(holder.itemView.context.resources.getString(R.string.ordered_at))
            builder.append(str1)
            val str2 = SpannableString(orderDate)
            builder.append(str2)
            if (!orderList[position].distance.equals("null")) {
                val str3 = SpannableString(" away from ")
                val boldSpan3 = StyleSpan(Typeface.BOLD)
                str3.setSpan(
                    boldSpan3, 0, str3.length, 0
                )
                builder.append(str3)
                val suffix = if(orderList[position].distance.toDouble()/1000 <1){
                    "m"
                }else{
                    "km"
                }
                val strDistance = SpannableString(dformat.format(orderList[position].distance.toDouble())+suffix)
                if (orderList[position].distance.toDouble() > 500) {
                    strDistance.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(holder.itemView.context, R.color.red)),
                        0,
                        strDistance.length,
                        0
                    )
                } else {
                    strDistance.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(holder.itemView.context, R.color.cnl_color_2)),
                        0,
                        strDistance.length,
                        0
                    )
                }
                val boldSpan4 = StyleSpan(Typeface.BOLD)
                strDistance.setSpan(
                    boldSpan4, 0, strDistance.length, 0
                )
                builder.append(strDistance)
            }
            holder.orderAt.setText(builder)
        }else{
            holder.orderAt.visibility = View.GONE
        }

        holder.addMore.setOnClickListener {
            mListener.onEdit(orderList[position])
        }

        holder.editItem.setOnClickListener {
            mListener.onEdit(orderList[position])
        }

        if (from.equals("summary")){
            holder.editItem.visibility = View.GONE
            holder.downloadChalan.visibility = View.GONE
            holder.addMore.visibility = View.INVISIBLE
            if (!orderList[position].orderStatus.equals("null")) {
                holder.statusLayout.visibility = View.VISIBLE
                if (orderList[position].orderStatus.equals("PENDING")) {
                    holder.tvOrderStatus.text = holder.itemView.resources.getString(R.string.pending)
                    holder.statusLayout.background.setTint(holder.itemView.resources.getColor(R.color.status_pending_stroke))
                    val gd = GradientDrawable()
                    gd.setColor(holder.itemView.resources.getColor(R.color.status_pending))
                    gd.cornerRadius = 5f
                    gd.setStroke(2, holder.itemView.resources.getColor(R.color.white))
                    holder.tvOrderStatus.setBackgroundDrawable(gd)
                } else if (orderList[position].orderStatus.equals("DELIVERED")) {
                    holder.tvOrderStatus.text = holder.itemView.resources.getString(R.string.delivered)
                    holder.statusLayout.background.setTint(holder.itemView.resources.getColor(R.color.status_delivered_stroke))
                    val gd = GradientDrawable()
                    gd.setColor(holder.itemView.resources.getColor(R.color.status_delivered))
                    gd.cornerRadius = 5f
                    gd.setStroke(2, holder.itemView.resources.getColor(R.color.white))
                    holder.tvOrderStatus.setBackgroundDrawable(gd)
                }
            }
        }

        holder.downloadChalan.setOnClickListener {
            ApiServices.apiGETInputStream(Api.get_chalan_download+"?order_no="+orderList[position].orderId, queue, holder.itemView.context, object : ApiServiceListener{
                override fun onResponseSuccess(response: String) {
                    TODO("Not yet implemented")
                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                    TODO("Not yet implemented")
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    TODO("Not yet implemented")
                }

                override fun onResponseFailure(error: VolleyError) {
                        ViewUtils.getErrorResponse(error, holder.itemView.context)
                }

                override fun onException(e: Exception) {
                    e.printStackTrace()
                }

            })
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                val filteredList: ArrayList<OrderList> = ArrayList()
                if (charString.isEmpty()) {
                    orderList = mValues
                } else {
                    //val filteredList: ArrayList<RetailShops> = ArrayList<RetailShops>()
                    for (row in mValues) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.outletName.toLowerCase().contains(charString.lowercase(Locale.getDefault()))) {
                            filteredList.add(row)
                        }
                    }
                    //itemList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                orderList = results.values as List<OrderList>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView
        internal val routeName: TextView
        internal val orderAt: TextView
        internal val subTotal: TextView
        internal val addMore: TextView
        internal val editItem: ImageView
        internal val downloadChalan: ImageView
        internal val productList: RecyclerView
        internal val statusLayout: LinearLayout
        internal val tvOrderStatus: TextView

        init {
            shopName = itemView.findViewById(R.id.tvShopName)
            routeName = itemView.findViewById(R.id.tvRouteName)
            orderAt = itemView.findViewById(R.id.tvOrderDate)
            subTotal = itemView.findViewById(R.id.tvSubTotal)
            productList = itemView.findViewById(R.id.productlist)
            addMore = itemView.findViewById(R.id.tvAddMore)
            editItem = itemView.findViewById(R.id.btn_edit)
            downloadChalan = itemView.findViewById(R.id.btn_download)
            statusLayout = itemView.findViewById(R.id.layoutStatus2)
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus)

        }
    }


}