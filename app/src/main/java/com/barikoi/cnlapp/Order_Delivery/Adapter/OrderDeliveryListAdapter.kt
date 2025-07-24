package com.barikoi.cnlapp.Order_Delivery.Adapter

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
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.order_create.Adapter.ConfirmOrderProductListAdapter
import com.barikoi.cnlapp.order_create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.order_create.RoomDB.OrderList
import com.barikoi.cnlapp.utils.extension.totalAmountFormatted
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDeliveryListAdapter(
    var mValues: List<OrderList>,
    var mListener: OnEditOrderListener,
    var from: String
) : RecyclerView.Adapter<OrderDeliveryListAdapter.ViewHolder>(),
    Filterable {
    var orderList: List<OrderList> = mValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_order_delivery_status_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mItem = orderList[position]
        holder.routeName.text = mItem.routeName
        holder.shopName.text = mItem.outletName
        holder.subTotal.text = mItem.grandTotal.totalAmountFormatted()

        if (mItem.brands_array.isNotEmpty()) {
            val adapter = ConfirmOrderProductListAdapter(mItem.brands_array)
            holder.productList.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        if (!mItem.orderStatus.equals("null")) {
            if (mItem.orderStatus.equals("PENDING")) {
                holder.orderStatus.text = holder.itemView.resources.getString(R.string.pending)
                holder.layoutStatus.background.setTint(holder.itemView.resources.getColor(R.color.status_pending_stroke))
                val gd = GradientDrawable()
                gd.setColor(holder.itemView.resources.getColor(R.color.status_pending))
                gd.cornerRadius = 5f
                gd.setStroke(2, holder.itemView.resources.getColor(R.color.white))
                holder.orderStatus.setBackgroundDrawable(gd)
            } else if (mItem.orderStatus.equals("DELIVERED")) {
                holder.orderStatus.text = holder.itemView.resources.getString(R.string.delivered)
                holder.layoutStatus.background.setTint(holder.itemView.resources.getColor(R.color.status_delivered_stroke))
                val gd = GradientDrawable()
                gd.setColor(holder.itemView.resources.getColor(R.color.status_delivered))
                gd.cornerRadius = 5f
                gd.setStroke(2, holder.itemView.resources.getColor(R.color.white))
                holder.orderStatus.setBackgroundDrawable(gd)
            } else {
                holder.orderStatus.text = holder.itemView.resources.getString(R.string.bounced)
                holder.layoutStatus.background.setTint(holder.itemView.resources.getColor(R.color.status_bounced_stroke))
                val gd = GradientDrawable()
                gd.setColor(holder.itemView.resources.getColor(R.color.status_bounced))
                gd.cornerRadius = 5f
                gd.setStroke(2, holder.itemView.resources.getColor(R.color.white))
                holder.orderStatus.setBackgroundDrawable(gd)
            }
        }

        if (!mItem.orderedAt.equals("null")) {
            holder.orderAt.visibility = View.VISIBLE
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(mItem.orderedAt))
            var dformat = DecimalFormat("#.##")
            val builder = SpannableStringBuilder()
            val str1 =
                SpannableString(holder.itemView.context.resources.getString(R.string.ordered_at))
            builder.append(str1)
            val str2 = SpannableString(orderDate)
            builder.append(str2)
            if (!mItem.distance.equals("null")) {
                val str3 = SpannableString(" away from ")
                val boldSpan3 = StyleSpan(Typeface.BOLD)
                str3.setSpan(
                    boldSpan3, 0, str3.length, 0
                )
                builder.append(str3)
                val suffix = if (orderList[position].distance.toDouble() / 1000 < 1) {
                    dformat.format(orderList[position].distance.toDouble()) + "m"
                } else {
                    dformat.format(orderList[position].distance.toDouble() / 1000) + "km"
                }
                val strDistance = SpannableString(suffix)
                if (mItem.distance.toDouble() > 100) {
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
            holder.orderAt.setText(builder)
        } else {
            holder.orderAt.visibility = View.GONE
        }

        if (from.equals("SO", true)) {
            if (mItem.orderStatus.equals("PENDING")) {
                holder.editItem.visibility = View.VISIBLE
            } else {
                holder.editItem.visibility = View.GONE
            }
        } else {
            holder.editItem.visibility = View.GONE
        }


        holder.editItem.setOnClickListener {
            mListener.onEdit(mItem)
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
                        if (row.outletName.toLowerCase()
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
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
        internal val shopName: TextView = itemView.findViewById(R.id.tvShopName)
        internal val routeName: TextView = itemView.findViewById(R.id.tvRouteName)
        internal val orderAt: TextView = itemView.findViewById(R.id.tvOrderDate)
        internal val subTotal: TextView = itemView.findViewById(R.id.tvSubTotal)
        internal val editItem: ImageView = itemView.findViewById(R.id.btn_edit)
        internal val layoutStatus: LinearLayout = itemView.findViewById(R.id.layoutStatus2)
        internal val orderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        internal val productList: RecyclerView = itemView.findViewById(R.id.productlist)
    }
}