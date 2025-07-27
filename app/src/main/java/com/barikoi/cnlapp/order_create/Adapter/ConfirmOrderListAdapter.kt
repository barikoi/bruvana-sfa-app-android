package com.barikoi.cnlapp.order_create.Adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.order_create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.order_create.RoomDB.OrderList
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.format
import com.barikoi.cnlapp.utils.extension.formateDateYY
import org.json.JSONObject
import java.util.Locale

class ConfirmOrderListAdapter(
    var mListener: OnEditOrderListener,
    var from: String
) : RecyclerView.Adapter<ConfirmOrderListAdapter.ViewHolder>(), Filterable {
    var mValues: List<OrderList> = emptyList()
    var orderList: List<OrderList> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_confirm_order_view, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(orderList: List<OrderList>) {
        this.orderList = orderList
        mValues = orderList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.routeName.text = orderList[position].routeName
        holder.shopName.text = orderList[position].outletName

        if (orderList[position].grandTotal == "null") {
            holder.subTotal.text = "0.00"
        } else {
            holder.subTotal.text = orderList[position].grandTotal.toDouble().toString().format()
        }

        if (orderList[position].brands_array.isNotEmpty()) {
            val adapter = ConfirmOrderProductListAdapter(orderList[position].brands_array)
            holder.productList.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        if (orderList[position].orderedAt != "null") {
            holder.orderAt.visibility = View.VISIBLE
            val orderDate = orderList[position].orderedAt.formateDateYY()
            val builder = SpannableStringBuilder()
            val str1 =
                SpannableString(holder.itemView.context.resources.getString(R.string.ordered_at))
            builder.append(str1)
            val str2 = SpannableString(orderDate)
            builder.append(str2)
            if (orderList[position].distance != "null") {
                val str3 = SpannableString(holder.itemView.context.getString(R.string.away_from))
                val boldSpan3 = StyleSpan(Typeface.BOLD)
                str3.setSpan(
                    boldSpan3, 0, str3.length, 0
                )
                builder.append(str3)
                val suffix = if (orderList[position].distance.toDouble() / 1000 < 1) {
                    orderList[position].distance.toDouble().toString().format() + "m"
                } else {
                    (orderList[position].distance.toDouble() / 1000).toString().format() + "km"
                }
                val strDistance = SpannableString(suffix)
                if (orderList[position].distance.toDouble() > 500) {
                    strDistance.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.red
                            )
                        ),
                        0,
                        strDistance.length,
                        0
                    )
                } else {
                    strDistance.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.cnl_color_2
                            )
                        ),
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
            holder.orderAt.text = builder
        } else {
            holder.orderAt.visibility = View.GONE
        }

        holder.addMore.setOnClickListener {
            mListener.onEdit(orderList[position])
        }

        holder.editItem.setOnClickListener {
            mListener.onEdit(orderList[position])
        }

        if (from == "summary") {
            holder.editItem.visibility = View.GONE
            holder.downloadChalan.visibility = View.GONE
            holder.addMore.visibility = View.INVISIBLE
            if (orderList[position].orderStatus != "null") {
                holder.statusLayout.visibility = View.VISIBLE
                if (orderList[position].orderStatus == "PENDING") {
                    holder.tvOrderStatus.text =
                        holder.itemView.resources.getString(R.string.pending)
                    holder.statusLayout.background.setTint(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.status_pending_stroke
                        )
                    )
                    val gd = GradientDrawable()
                    gd.setColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.status_pending
                        )
                    )
                    gd.cornerRadius = 5f
                    gd.setStroke(
                        2,
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.white
                        )
                    )
                    holder.tvOrderStatus.background = gd
                } else if (orderList[position].orderStatus == "DELIVERED") {
                    holder.tvOrderStatus.text =
                        holder.itemView.resources.getString(R.string.delivered)
                    holder.statusLayout.background.setTint(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.status_delivered_stroke
                        )
                    )
                    val gd = GradientDrawable()
                    gd.setColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.status_delivered
                        )
                    )
                    gd.cornerRadius = 5f
                    gd.setStroke(
                        2,
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.white
                        )
                    )
                    holder.tvOrderStatus.background = gd
                }
            }
        }

        holder.downloadChalan.setOnClickListener {
            ApiServices.apiGETInputStream(
                Api.get_chalan_download + "?order_no=" + orderList[position].orderId,
                holder.itemView.context,
                object : ApiServiceListener {
                    override fun onResponseSuccess(response: String) {}

                    override fun onJSONResponseSuccess(response: JSONObject) {}

                    override fun onNetworkResponseSuccess(response: NetworkResponse) {}

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
                    for (row in mValues) {
                        if (row.outletName.lowercase()
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(row)
                        }
                    }
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
        internal val shopName: TextView = itemView.findViewById(R.id.tvShopName)
        internal val routeName: TextView = itemView.findViewById(R.id.tvRouteName)
        internal val orderAt: TextView = itemView.findViewById(R.id.tvOrderDate)
        internal val subTotal: TextView = itemView.findViewById(R.id.tvSubTotal)
        internal val addMore: TextView = itemView.findViewById(R.id.tvAddMore)
        internal val editItem: ImageView = itemView.findViewById(R.id.btn_edit)
        internal val downloadChalan: ImageView = itemView.findViewById(R.id.btn_download)
        internal val productList: RecyclerView = itemView.findViewById(R.id.productlist)
        internal val statusLayout: LinearLayout = itemView.findViewById(R.id.layoutStatus2)
        internal val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
    }
}