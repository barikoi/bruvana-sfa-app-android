package com.barikoi.cnlapp.Notice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.barikoi.cnlapp.R
import java.text.SimpleDateFormat
import java.util.Locale


class NoticeListAdapter(val notices: List<Notice>) :
    RecyclerView.Adapter<NoticeListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.single_notice_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mItem = notices[position]

        holder.userName.text = mItem.senderName
        holder.tvDesignation.text = mItem.designation
        holder.noticeMessage.text = mItem.message

        if (mItem.updated_at != "null") {
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(mItem.updated_at))
            holder.noticeDate.text = orderDate
        }

        if (mItem.imageUrl.isNotEmpty() && mItem.imageUrl != "null") {
            holder.imageUser.load(
                mItem.imageUrl
            )
        } else {
            holder.imageUser.visibility = View.GONE
        }

        var expandable = false

        holder.noticeMessage.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //Log.d("Notice", "Line Count: "+holder.noticeMessage.lineCount)
                if (holder.noticeMessage.lineCount > 2) {
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
                    holder.noticeMessage.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })


        holder.seeMore.setOnClickListener {
            if (!expandable) {
                expandable = true
                holder.noticeMessage.maxLines = Integer.MAX_VALUE
                holder.seeMore.visibility = View.VISIBLE
                holder.seeMore.text = holder.itemView.resources.getString(R.string.see_less)
            } else {
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
        internal val tvDesignation: TextView = itemView.findViewById(R.id.tvDesignation)
        internal val userName: TextView = itemView.findViewById(R.id.userName)
        internal val noticeDate: TextView = itemView.findViewById(R.id.noticeDate)
        internal val noticeMessage: TextView = itemView.findViewById(R.id.tvNotice)
        internal val seeMore: TextView = itemView.findViewById(R.id.tvSeeMore)
        internal val imageUser: ImageView = itemView.findViewById(R.id.imageUser)
    }
}