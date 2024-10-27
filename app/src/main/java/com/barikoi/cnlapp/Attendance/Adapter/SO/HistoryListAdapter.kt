package com.barikoi.cnlapp.Attendance.Adapter.SO

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.R
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale


class HistoryListAdapter(private val histories: List<HistoryList>) :
    RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_attendance_history, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val _sdfWatchMonth = SimpleDateFormat("LLL", Locale.getDefault())
        val _sdfWatchDate = SimpleDateFormat("dd", Locale.getDefault())
        val _sdfWatchtime = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val mItem = histories[position]
        if (mItem.enterTime.isNotEmpty() && !mItem.enterTime.equals("null")) {
            holder.textViewMonth.text = _sdfWatchMonth.format(oldDate.parse(mItem.enterTime))
            holder.textViewDate.text = _sdfWatchDate.format(oldDate.parse(mItem.enterTime))
            holder.inTime.text = _sdfWatchtime.format(oldDate.parse(mItem.enterTime))
        } else {
            holder.inTime.text = "--:--"
        }

        if (mItem.exitTime.isNotEmpty() && !mItem.exitTime.equals("null")) {
            holder.outTime.text = _sdfWatchtime.format(oldDate.parse(mItem.exitTime))
        } else {
            holder.outTime.text = "--:--"
        }

        if (mItem.routeName.isNotEmpty() && !mItem.routeName.equals("null")) {
            holder.marketName.text = holder.itemView.resources.getString(R.string.market) + " " + mItem.routeName
        } else {
            holder.marketName.visibility = View.GONE
        }

        if (mItem.imageLink.isNotEmpty() && !mItem.imageLink.equals("null")) {
            Glide.with(holder.itemView.context)
                .load(mItem.imageLink)
                .into(holder.imageUser)
        } else {
            holder.imageUser.visibility = View.GONE
        }


        if (mItem.checkInAddress.isNotEmpty() && !mItem.checkInAddress.equals("null")) {
            holder.inAddress.text = mItem.checkInAddress
        } else {
            holder.inAddress.text = ""
        }
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val textViewMonth: TextView
        internal val textViewDate: TextView
        internal val marketName: TextView
        internal val inTime: TextView
        internal val outTime: TextView
        internal val inAddress: TextView
        internal val outAddress: TextView
        internal val imageUser: ImageView

        init {
            textViewMonth = itemView.findViewById(R.id.tvMonth)
            textViewDate = itemView.findViewById(R.id.tvDate)
            marketName = itemView.findViewById(R.id.marketName)
            inTime = itemView.findViewById(R.id.inTime)
            outTime = itemView.findViewById(R.id.outTime)
            inAddress = itemView.findViewById(R.id.inAddress)
            outAddress = itemView.findViewById(R.id.outAddress)
            imageUser = itemView.findViewById(R.id.imageUser)
        }
    }
}