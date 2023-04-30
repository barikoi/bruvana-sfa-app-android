package com.barikoi.cnlapp.Notice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
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
        holder.noticeMessage.text = mItem.message

        if (!mItem.updated_at.equals("null")){
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(mItem.updated_at))
            holder.noticeDate.setText(orderDate)
        }

        if (!mItem.imageUrl.isNullOrEmpty() && !mItem.imageUrl.equals("null")){
            Glide.with(holder.itemView.context)
                .load(mItem.imageUrl)
                .into(holder.imageUser)
        }else{
            holder.imageUser.visibility = View.GONE
        }



        var expandable = false

        holder.noticeMessage.getViewTreeObserver().addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //Log.d("Notice", "Line Count: "+holder.noticeMessage.lineCount)
                if (holder.noticeMessage.lineCount >2){
                    /*Log.d("Notice", "Line Count: "+holder.noticeMessage.lineCount+" ellipse: "+holder.noticeMessage.layout.getEllipsisCount(holder.noticeMessage.lineCount))
                    if (holder.noticeMessage.layout.getEllipsisCount(holder.noticeMessage.lineCount-1) > 3){
                        holder.seeMore.visibility = View.VISIBLE
                        holder.seeMore.text = holder.itemView.resources.getString(R.string.see_more)
                    }
                    else{
                        holder.seeMore.visibility = View.GONE
                    }*/
                    holder.seeMore.visibility = View.VISIBLE
                    holder.seeMore.text = holder.itemView.resources.getString(R.string.see_more)
                    holder.noticeMessage.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                }
            }
        })


        holder.seeMore.setOnClickListener {
            if (!expandable){
                expandable = true
                holder.noticeMessage.maxLines = Integer.MAX_VALUE
                holder.seeMore.visibility = View.VISIBLE
                holder.seeMore.text = holder.itemView.resources.getString(R.string.see_less)
            }else{
                expandable = false
                holder.noticeMessage.maxLines = 3
                holder.seeMore.text = holder.itemView.resources.getString(R.string.see_more)
                holder.seeMore.visibility = View.VISIBLE
            }
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