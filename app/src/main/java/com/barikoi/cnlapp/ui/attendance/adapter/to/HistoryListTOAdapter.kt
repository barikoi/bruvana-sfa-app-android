package com.barikoi.cnlapp.ui.attendance.adapter.to

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.ui.attendance.model.HistoryList
import java.text.SimpleDateFormat
import java.util.Locale


class HistoryListTOAdapter(val histories: List<HistoryList>) :
    RecyclerView.Adapter<HistoryListTOAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_attendance_history_to, parent, false)
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
        if (mItem.enterTime.isNotEmpty() && mItem.enterTime != "null") {
            holder.textViewMonth.text = _sdfWatchMonth.format(oldDate.parse(mItem.enterTime))
            holder.textViewDate.text = _sdfWatchDate.format(oldDate.parse(mItem.enterTime))
            holder.inTime.text = _sdfWatchtime.format(oldDate.parse(mItem.enterTime))
        } else {
            holder.inTime.text = "--:--"
        }

        if (mItem.exitTime.isNotEmpty() && mItem.exitTime != "null") {
            holder.outTime.text = _sdfWatchtime.format(oldDate.parse(mItem.exitTime))
        } else {
            holder.outTime.text = "--:--"
        }

        if (mItem.routeName.isNotEmpty() && mItem.routeName != "null") {
            holder.marketName.text = holder.itemView.resources.getString(R.string.market) + " " + mItem.routeName
        } else {
            holder.marketName.visibility = View.GONE
        }

        if (mItem.imageLink.isNotEmpty() && mItem.imageLink != "null") {
            holder.imageUser.load(
                mItem.imageLink
            )
        } else {
            holder.imageUser.visibility = View.GONE
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
        internal val marketName: TextView = itemView.findViewById(R.id.marketName)
        internal val userName: TextView = itemView.findViewById(R.id.soName)
        internal val inTime: TextView = itemView.findViewById(R.id.inTime)
        internal val outTime: TextView = itemView.findViewById(R.id.outTime)
        internal val inAddress: TextView = itemView.findViewById(R.id.inAddress)
        internal val outAddress: TextView = itemView.findViewById(R.id.outAddress)
        internal val imageUser: ImageView = itemView.findViewById(R.id.imageUser)
    }
}