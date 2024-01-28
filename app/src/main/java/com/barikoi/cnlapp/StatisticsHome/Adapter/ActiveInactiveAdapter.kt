package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.ActiveInactiveSO
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveInactiveAdapter(val histories: List<ActiveInactiveSO>) : RecyclerView.Adapter<ActiveInactiveAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActiveInactiveAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_active_inactive_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ActiveInactiveAdapter.ViewHolder, position: Int) {
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val _sdfWatchMonth = SimpleDateFormat("LLL", Locale.ENGLISH)
        val _sdfWatchDate = SimpleDateFormat("dd", Locale.ENGLISH)
        val _sdfWatchtime = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        val mItem = histories[position]
        if (!mItem.userName.equals("null")){
            holder.userName.setText(mItem.userName)
        }else{
            holder.userName.visibility = View.GONE
        }

        if (!mItem.lastUpdatedTime.isNullOrEmpty() && !mItem.lastUpdatedTime.equals("null")){
            holder.textViewMonth.setText(_sdfWatchMonth.format(oldDate.parse(mItem.lastUpdatedTime)))
            holder.textViewDate.setText(_sdfWatchDate.format(oldDate.parse(mItem.lastUpdatedTime)))
            holder.updatedTime.setText(_sdfWatchtime.format(oldDate.parse(mItem.lastUpdatedTime)))
        }else{
            holder.updatedTime.setText("--:--")
        }
        if (!mItem.imageLink.isNullOrEmpty() && !mItem.imageLink.equals("null")){
            Glide.with(holder.itemView.context)
                .load(mItem.imageLink)
                .into(holder.imageUser)
        }else{
            holder.imageUser.visibility = View.GONE
        }

        if (mItem.status.equals("active", true)){
            holder.imageStatus.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_active))
        }else{
            holder.imageStatus.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_inactive))
        }

        if (!mItem.checkInAddress.isNullOrEmpty() && !mItem.checkInAddress.equals("null")){
            holder.inAddress.setText(mItem.checkInAddress)
        }else{
            holder.inAddress.setText("")
        }
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val textViewMonth: TextView
        internal val textViewDate: TextView
        internal val userName: TextView
        internal val updatedTime: TextView
        internal val inAddress: TextView
        internal val imageUser: ImageView
        internal val imageStatus: ImageView
        init {
            textViewMonth = itemView.findViewById(R.id.tvMonth)
            textViewDate = itemView.findViewById(R.id.tvDate)
            userName = itemView.findViewById(R.id.soName)
            updatedTime = itemView.findViewById(R.id.updatedTime)
            inAddress = itemView.findViewById(R.id.lastAddress)
            imageUser = itemView.findViewById(R.id.imageUser)
            imageStatus = itemView.findViewById(R.id.imageStatus)
        }
    }
}