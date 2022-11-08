package com.barikoi.cnlapp.Attendance.Adapter

import android.graphics.BitmapFactory
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class HistoryListAdapter (val histories: List<HistoryList>) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_attendance_history, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val _sdfWatchMonth = SimpleDateFormat("LLL", Locale.ENGLISH)
        val _sdfWatchDate = SimpleDateFormat("dd", Locale.ENGLISH)
        val _sdfWatchtime = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        val mItem = histories[position]
        if (!mItem.enterTime.isNullOrEmpty() && !mItem.enterTime.equals("null")){
            holder.textViewMonth.setText(_sdfWatchMonth.format(oldDate.parse(mItem.enterTime)))
            holder.textViewDate.setText(_sdfWatchDate.format(oldDate.parse(mItem.enterTime)))
            holder.inTime.setText(_sdfWatchtime.format(oldDate.parse(mItem.enterTime)))
        }else{
            holder.inTime.setText("--:--")
        }

        if (!mItem.exitTime.isNullOrEmpty() && !mItem.exitTime.equals("null")){
            holder.outTime.setText(_sdfWatchtime.format(oldDate.parse(mItem.exitTime)))
        }else{
            holder.outTime.setText("--:--")
        }

        if (!mItem.routeName.isNullOrEmpty() && !mItem.routeName.equals("null")){
            holder.marketName.setText(mItem.routeName)
        }else{
            holder.marketName.setText("")
        }

        if (!mItem.imageLink.isNullOrEmpty() && !mItem.imageLink.equals("null")){
            /*val newurl = URL(Api.base_url+java.net.URLEncoder.encode(mItem.imageLink, "UTF-8"))
            val bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream())
            holder.imageUser.setImageBitmap(bitmap)*/
            Glide.with(holder.itemView.context)
                .load(Api.base_url+mItem.imageLink)
                .into(holder.imageUser)
        }else{
            holder.imageUser.visibility = View.GONE
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