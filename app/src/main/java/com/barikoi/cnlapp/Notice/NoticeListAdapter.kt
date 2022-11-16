package com.barikoi.cnlapp.Notice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.OutletStatistics
import com.barikoi.cnlapp.Utils.Api
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class NoticeListAdapter (val notices: List<Notice>) : RecyclerView.Adapter<NoticeListAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoticeListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_notice_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: NoticeListAdapter.ViewHolder, position: Int) {
        val mItem = notices[position]

        holder.userName.text = mItem.senderName
        holder.tvDesignation.text = mItem.designation
        if (!mItem.updated_at.equals("null")){
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(mItem.updated_at))
            holder.noticeDate.setText(orderDate)
        }

        if (!mItem.imageUrl.isNullOrEmpty() && !mItem.imageUrl.equals("null")){
            Glide.with(holder.itemView.context)
                .load(Api.base_url+mItem.imageUrl)
                .into(holder.imageUser)
        }else{
            holder.imageUser.visibility = View.GONE
        }

        holder.seeMore.setOnClickListener {
            holder.seeMore.text = holder.itemView.resources.getString(R.string.see_less)
        }
    }

    override fun getItemCount(): Int {
        return notices.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val tvDesignation: TextView
        internal val userName: TextView
        internal val noticeDate: TextView
        internal val noticeMessage: TextView
        internal val seeMore: TextView
        internal val imageUser: ImageView

        init {
            tvDesignation = itemView.findViewById(R.id.tvDesignation)
            userName = itemView.findViewById(R.id.userName)
            imageUser = itemView.findViewById(R.id.imageUser)
            noticeDate = itemView.findViewById(R.id.noticeDate)
            noticeMessage = itemView.findViewById(R.id.tvNotice)
            seeMore = itemView.findViewById(R.id.tvSeeMore)
        }
    }
}