package com.barikoi.cnlapp.ui.active_inactive

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.data.remote.models.ActiveInactiveUser
import com.barikoi.cnlapp.databinding.ItemActiveInactiveBinding

class AdapterActiveInactive :
    RecyclerView.Adapter<AdapterActiveInactive.ActiveInactiveViewHolder>() {
    private var activeInactiveList: List<ActiveInactiveUser> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActiveInactiveViewHolder {
        return ActiveInactiveViewHolder(
            ItemActiveInactiveBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ActiveInactiveViewHolder,
        position: Int
    ) {
        val  activeInactiveUser = activeInactiveList[position]

//        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
//        val _sdfWatchMonth = SimpleDateFormat("LLL", Locale.ENGLISH)
//        val _sdfWatchDate = SimpleDateFormat("dd", Locale.ENGLISH)
//        val _sdfWatchtime = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
//
//        val mItem = histories[position]
//        if (!mItem.userName.equals("null")){
//            holder.userName.setText(mItem.userName)
//        }else{
//            holder.userName.visibility = View.GONE
//        }
//
//        if (!mItem.lastUpdatedTime.isNullOrEmpty() && !mItem.lastUpdatedTime.equals("null")){
//            holder.textViewMonth.setText(_sdfWatchMonth.format(oldDate.parse(mItem.lastUpdatedTime)))
//            holder.textViewDate.setText(_sdfWatchDate.format(oldDate.parse(mItem.lastUpdatedTime)))
//            holder.updatedTime.setText(_sdfWatchtime.format(oldDate.parse(mItem.lastUpdatedTime)))
//        }else{
//            holder.updatedTime.setText("--:--")
//        }
//        if (!mItem.imageLink.isNullOrEmpty() && !mItem.imageLink.equals("null")){
//            Glide.with(holder.itemView.context)
//                .load(mItem.imageLink)
//                .into(holder.imageUser)
//        }else{
//            holder.imageUser.visibility = View.GONE
//        }
//
//        if (mItem.status.equals("active", true)){
//            holder.imageStatus.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_active))
//        }else{
//            holder.imageStatus.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_inactive))
//        }
//
//        if (!mItem.checkInAddress.isNullOrEmpty() && !mItem.checkInAddress.equals("null")){
//            holder.inAddress.setText(mItem.checkInAddress)
//        }else{
//            holder.inAddress.setText("")
//        }
        
        holder.binding.apply {
            tvName.text = activeInactiveUser.userName
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<ActiveInactiveUser>) {
        activeInactiveList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = activeInactiveList.size

    inner class ActiveInactiveViewHolder(val binding: ItemActiveInactiveBinding) :
        RecyclerView.ViewHolder(binding.root)
}