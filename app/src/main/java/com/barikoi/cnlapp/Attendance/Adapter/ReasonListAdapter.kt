package com.barikoi.cnlapp.Attendance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.R
import java.text.SimpleDateFormat
import java.util.*

class ReasonListAdapter(val reasons: List<Pair<String, String>>) : RecyclerView.Adapter<ReasonListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReasonListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_attendance_reason_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ReasonListAdapter.ViewHolder, position: Int) {
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val _sdfWatchMonth = SimpleDateFormat("LLL", Locale.ENGLISH)
        val _sdfWatchDate = SimpleDateFormat("dd", Locale.ENGLISH)
        if (!reasons[position].first.isNullOrEmpty() && !reasons[position].first.equals("null")){
            holder.textViewMonth.setText(_sdfWatchMonth.format(oldDate.parse(reasons[position].first)))
            holder.textViewDate.setText(_sdfWatchDate.format(oldDate.parse(reasons[position].first)))
        }
        if (!reasons[position].second.equals("null")){
            holder.tvReason.setText(reasons[position].second)
        }
    }

    override fun getItemCount(): Int {
        return reasons.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val textViewMonth: TextView
        internal val textViewDate: TextView
        internal val tvReason: TextView

        init {
            textViewMonth = itemView.findViewById(R.id.tvMonth)
            textViewDate = itemView.findViewById(R.id.tvDate)
            tvReason = itemView.findViewById(R.id.reason)
        }
    }
}