package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.ActiveInactiveSO
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveInactiveAdapter(val histories: List<ActiveInactiveSO>) :
    RecyclerView.Adapter<ActiveInactiveAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_active_inactive_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val _sdfWatchMonth = SimpleDateFormat("LLL", Locale.ENGLISH)
        val _sdfWatchDate = SimpleDateFormat("dd", Locale.ENGLISH)
        val _sdfWatchtime = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        val mItem = histories[position]
        if (mItem.userName != "null") {
            holder.userName.text = mItem.userName
        } else {
            holder.userName.visibility = View.GONE
        }

        if (mItem.lastUpdatedTime.isNotEmpty() && mItem.lastUpdatedTime != "null") {
            holder.textViewMonth.text = _sdfWatchMonth.format(oldDate.parse(mItem.lastUpdatedTime))
            holder.textViewDate.text = _sdfWatchDate.format(oldDate.parse(mItem.lastUpdatedTime))
            holder.updatedTime.text = _sdfWatchtime.format(oldDate.parse(mItem.lastUpdatedTime))
        } else {
            holder.updatedTime.text = "--:--"
        }
        if (mItem.imageLink.isNotEmpty() && mItem.imageLink != "null") {
            holder.imageUser.load(
                mItem.imageLink
            )
        } else {
            holder.imageUser.visibility = View.GONE
        }

        if (mItem.status.equals("active", true)) {
            holder.imageStatus.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_active))
        } else {
            holder.imageStatus.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_inactive))
        }

        if (mItem.checkInAddress.isNotEmpty() && mItem.checkInAddress != "null") {
            holder.inAddress.text = mItem.checkInAddress
        } else {
            holder.inAddress.text = ""
        }
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val textViewMonth: TextView = itemView.findViewById(R.id.tvMonth)
        internal val textViewDate: TextView = itemView.findViewById(R.id.tvDate)
        internal val userName: TextView = itemView.findViewById(R.id.soName)
        internal val updatedTime: TextView = itemView.findViewById(R.id.updatedTime)
        internal val inAddress: TextView = itemView.findViewById(R.id.lastAddress)
        internal val imageUser: ImageView = itemView.findViewById(R.id.imageUser)
        internal val imageStatus: ImageView = itemView.findViewById(R.id.imageStatus)
    }
}